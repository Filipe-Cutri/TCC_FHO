class EstablishmentProfessionalsManager {
    constructor() {
        this.apiBaseUrl = window.API_CONFIG?.baseUrl || 'https://localhost:8443';
        this.professionals = [];
        this.establishmentId = null;
        this.init();
    }

    init() {
        this.establishmentId = this.getEstablishmentId();
        if (!this.establishmentId) {
            this.showError('ID do estabelecimento não encontrado na sessão');
            return;
        }
        
        this.loadProfessionals();
        this.setupEventListeners();
    }

    getEstablishmentId() {
        const session = window.establishmentSession?.getSession();
        return session?.establishmentId || session?.id;
    }

    setupEventListeners() {
        const saveBtn = document.querySelector('#newProfessionalModal .establishment-btn-success');
        if (saveBtn) {
            saveBtn.addEventListener('click', () => this.saveProfessional());
        }

        const modal = document.getElementById('newProfessionalModal');
        if (modal) {
            modal.addEventListener('hidden.bs.modal', () => this.clearForm());
        }

        // File upload preview
        const fileInput = document.getElementById('professionalImageFile');
        if (fileInput) {
            fileInput.addEventListener('change', (e) => this.handleFileSelect(e));
        }

        const removeBtn = document.getElementById('removeProfessionalImage');
        if (removeBtn) {
            removeBtn.addEventListener('click', () => this.removeImagePreview());
        }
    }

    async loadProfessionals() {
        const grid = document.getElementById('professionalsGrid');
        if (!grid) return;

        try {
            const response = await fetch(`${this.apiBaseUrl}/api/establishment/professionals?establishmentId=${this.establishmentId}`);
            const data = await response.json();

            if (data.success && data.data) {
                this.professionals = data.data;
                this.renderProfessionals();
            } else {
                this.showError(data.message || 'Erro ao carregar profissionais');
            }
        } catch (error) {
            console.error('Error loading professionals:', error);
            this.showError('Erro ao conectar com o servidor');
        }
    }

    renderProfessionals() {
        const grid = document.getElementById('professionalsGrid');
        if (!grid) return;

        if (this.professionals.length === 0) {
            grid.innerHTML = `
                <div class="col-12">
                    <div class="establishment-card">
                        <div class="card-body text-center py-5">
                            <i class="fas fa-users fa-3x text-muted mb-3"></i>
                            <h5>Nenhum profissional cadastrado</h5>
                            <p class="text-muted">Clique em "Novo Profissional" para adicionar o primeiro profissional</p>
                        </div>
                    </div>
                </div>
            `;
            return;
        }

        grid.innerHTML = this.professionals.map(professional => this.createProfessionalCard(professional)).join('');
        this.attachCardEventListeners();
    }

    createProfessionalCard(professional) {
        const statusBadge = professional.status === 'ACTIVE' ? 
            '<span class="badge bg-success">Ativo</span>' : 
            '<span class="badge bg-secondary">Inativo</span>';

        const rating = professional.rating || 0;
        const stars = this.renderStars(rating);

        const imageUrl = professional.imageUrl || 'https://via.placeholder.com/150';
        
        return `
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="establishment-card h-100">
                    <div class="card-body">
                        <div class="text-center mb-3">
                            <img src="${this.escapeHtml(imageUrl)}" alt="${this.escapeHtml(professional.name)}" 
                                 class="rounded-circle" style="width: 80px; height: 80px; object-fit: cover;"
                                 onerror="this.src='https://via.placeholder.com/150'">
                        </div>
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <h5 class="card-title mb-0">
                                <i class="fas fa-user-tie text-primary me-2"></i>
                                ${this.escapeHtml(professional.name)}
                            </h5>
                            ${statusBadge}
                        </div>
                        
                        ${professional.email ? `
                        <p class="mb-2">
                            <i class="fas fa-envelope text-muted me-2"></i>
                            <small>${this.escapeHtml(professional.email)}</small>
                        </p>
                        ` : ''}
                        
                        ${professional.phone ? `
                        <p class="mb-2">
                            <i class="fas fa-phone text-muted me-2"></i>
                            <small>${this.escapeHtml(professional.phone)}</small>
                        </p>
                        ` : ''}
                        
                        ${professional.specialties ? `
                        <p class="mb-3">
                            <i class="fas fa-star text-muted me-2"></i>
                            <small>${this.escapeHtml(professional.specialties)}</small>
                        </p>
                        ` : ''}
                        
                        <div class="d-flex justify-content-between align-items-center mt-3 pt-3 border-top">
                            <div>
                                ${stars}
                                <small class="text-muted ms-2">(${professional.totalAppointments || 0} agendamentos)</small>
                            </div>
                        </div>
                        
                        <div class="d-flex gap-2 mt-3">
                            <button class="btn btn-sm btn-outline-primary flex-fill edit-professional" data-id="${professional.id}">
                                <i class="fas fa-edit me-1"></i>Editar
                            </button>
                            <button class="btn btn-sm btn-outline-danger delete-professional" data-id="${professional.id}">
                                <i class="fas fa-trash me-1"></i>Excluir
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    renderStars(rating) {
        const fullStars = Math.floor(rating);
        const hasHalfStar = rating % 1 >= 0.5;
        const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
        
        let stars = '';
        for (let i = 0; i < fullStars; i++) {
            stars += '<i class="fas fa-star text-warning"></i>';
        }
        if (hasHalfStar) {
            stars += '<i class="fas fa-star-half-alt text-warning"></i>';
        }
        for (let i = 0; i < emptyStars; i++) {
            stars += '<i class="far fa-star text-warning"></i>';
        }
        
        return stars;
    }

    attachCardEventListeners() {
        document.querySelectorAll('.edit-professional').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = e.currentTarget.getAttribute('data-id');
                this.editProfessional(id);
            });
        });

        document.querySelectorAll('.delete-professional').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = e.currentTarget.getAttribute('data-id');
                this.deleteProfessional(id);
            });
        });
    }

    async saveProfessional() {
        const id = document.getElementById('professionalId')?.value?.trim();
        const name = document.getElementById('professionalName')?.value?.trim();
        const email = document.getElementById('professionalEmail')?.value?.trim();
        const phone = document.getElementById('professionalPhone')?.value?.trim();
        const specialties = document.getElementById('professionalSpecialties')?.value?.trim();
        const imageUrl = document.getElementById('professionalImageUrl')?.value?.trim();
        const imageFile = document.getElementById('professionalImageFile')?.files[0];

        if (!name) {
            this.showError('Nome é obrigatório');
            return;
        }

        const professional = {
            name,
            email: email || null,
            phone: phone || null,
            specialties: specialties || null,
            establishmentId: this.establishmentId
        };

        try {
            let response;
            if (id) {
                // Update existing professional
                response = await fetch(`${this.apiBaseUrl}/api/establishment/professionals/${id}?establishmentId=${this.establishmentId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(professional)
                });
            } else {
                // Create new professional
                response = await fetch(`${this.apiBaseUrl}/api/establishment/professionals`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(professional)
                });
            }

            const data = await response.json();

            if (data.success) {
                const professionalId = data.data?.id;
                
                // Upload image file if provided
                if (imageFile && professionalId) {
                    await this.uploadProfessionalImageFile(professionalId, imageFile);
                }
                // Or update with URL if provided and no file
                else if (imageUrl && professionalId && !imageFile) {
                    await this.updateProfessionalImage(professionalId, imageUrl);
                }
                
                this.showSuccess(id ? 'Profissional atualizado com sucesso!' : 'Profissional cadastrado com sucesso!');
                this.closeModal();
                await this.loadProfessionals();
            } else {
                this.showError(data.message || 'Erro ao salvar profissional');
            }
        } catch (error) {
            console.error('Error saving professional:', error);
            this.showError('Erro ao conectar com o servidor');
        }
    }

    async uploadProfessionalImageFile(professionalId, file) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('establishmentId', this.establishmentId);

        try {
            const response = await fetch(`${this.apiBaseUrl}/api/files/professional/${professionalId}/upload`, {
                method: 'POST',
                body: formData
            });

            const data = await response.json();
            if (!data.success) {
                this.showError('Erro ao enviar imagem: ' + (data.message || 'Erro desconhecido'));
            }
        } catch (error) {
            console.error('Error uploading professional image:', error);
            this.showError('Erro ao enviar imagem');
        }
    }

    async updateProfessionalImage(id, imageUrl) {
        try {
            const response = await fetch(`${this.apiBaseUrl}/api/establishment/professionals/${id}/image?establishmentId=${this.establishmentId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ imageUrl })
            });

            const data = await response.json();
            if (!data.success) {
                console.error('Failed to update image:', data.message);
            }
        } catch (error) {
            console.error('Error updating professional image:', error);
        }
    }

    async editProfessional(id) {
        const professional = this.professionals.find(p => p.id == id);
        if (!professional) {
            this.showError('Profissional não encontrado');
            return;
        }

        // Populate form with professional data
        document.getElementById('professionalId').value = professional.id;
        document.getElementById('professionalName').value = professional.name || '';
        document.getElementById('professionalEmail').value = professional.email || '';
        document.getElementById('professionalPhone').value = professional.phone || '';
        document.getElementById('professionalSpecialties').value = professional.specialties || '';
        document.getElementById('professionalImageUrl').value = professional.imageUrl || '';

        // Update modal title
        document.getElementById('modalTitle').textContent = 'Editar Profissional';
        
        // Show modal
        const modal = new bootstrap.Modal(document.getElementById('newProfessionalModal'));
        modal.show();
    }

    async deleteProfessional(id) {
        if (!confirm('Tem certeza que deseja excluir este profissional?')) {
            return;
        }

        try {
            const response = await fetch(`${this.apiBaseUrl}/api/establishment/professionals/${id}?establishmentId=${this.establishmentId}`, {
                method: 'DELETE'
            });

            const data = await response.json();

            if (data.success) {
                this.showSuccess('Profissional excluído com sucesso!');
                await this.loadProfessionals();
            } else {
                this.showError(data.message || 'Erro ao excluir profissional');
            }
        } catch (error) {
            console.error('Error deleting professional:', error);
            this.showError('Erro ao conectar com o servidor');
        }
    }

    clearForm() {
        const fields = ['professionalId', 'professionalName', 'professionalEmail', 'professionalPhone', 'professionalSpecialties', 'professionalImageUrl'];
        fields.forEach(id => {
            const field = document.getElementById(id);
            if (field) field.value = '';
        });
        
        // Clear file input
        const fileInput = document.getElementById('professionalImageFile');
        if (fileInput) fileInput.value = '';
        
        // Hide preview
        this.removeImagePreview();
        
        // Reset modal title
        const modalTitle = document.getElementById('modalTitle');
        if (modalTitle) {
            modalTitle.textContent = 'Adicionar Novo Profissional';
        }
    }

    handleFileSelect(event) {
        const file = event.target.files[0];
        if (file) {
            // Validate file type
            const validTypes = ['image/jpeg', 'image/jpg', 'image/png'];
            if (!validTypes.includes(file.type)) {
                this.showError('Apenas arquivos JPG e PNG são permitidos');
                event.target.value = '';
                return;
            }

            // Validate file size (5MB)
            if (file.size > 5 * 1024 * 1024) {
                this.showError('Arquivo muito grande. Tamanho máximo: 5MB');
                event.target.value = '';
                return;
            }

            // Show preview
            const reader = new FileReader();
            reader.onload = (e) => {
                const preview = document.getElementById('professionalImagePreview');
                const img = document.getElementById('professionalPreviewImg');
                if (preview && img) {
                    img.src = e.target.result;
                    preview.style.display = 'block';
                }
            };
            reader.readAsDataURL(file);

            // Clear URL input when file is selected
            const urlInput = document.getElementById('professionalImageUrl');
            if (urlInput) urlInput.value = '';
        }
    }

    removeImagePreview() {
        const preview = document.getElementById('professionalImagePreview');
        const img = document.getElementById('professionalPreviewImg');
        const fileInput = document.getElementById('professionalImageFile');
        
        if (preview) preview.style.display = 'none';
        if (img) img.src = '';
        if (fileInput) fileInput.value = '';
    }

    closeModal() {
        const modal = bootstrap.Modal.getInstance(document.getElementById('newProfessionalModal'));
        if (modal) {
            modal.hide();
        }
    }

    showSuccess(message) {
        this.showNotification(message, 'success');
    }

    showError(message) {
        this.showNotification(message, 'danger');
    }

    showNotification(message, type) {
        const container = document.querySelector('.establishment-container');
        if (!container) {
            alert(message);
            return;
        }

        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        container.insertBefore(alertDiv, container.firstChild);

        setTimeout(() => {
            alertDiv.remove();
        }, 5000);
    }

    escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text ? text.replace(/[&<>"']/g, m => map[m]) : '';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    window.professionalsManager = new EstablishmentProfessionalsManager();
});
