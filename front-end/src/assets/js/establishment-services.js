class EstablishmentServicesManager {
    constructor() {
        // Use API_CONFIG if available, otherwise use relative URLs for same-origin requests
        if (window.API_CONFIG?.baseUrl !== undefined) {
            this.apiBaseUrl = window.API_CONFIG.baseUrl;
        } else {
            // Fallback: use relative URLs (works when frontend is served from backend)
            this.apiBaseUrl = '';
        }
        console.log('API Base URL:', this.apiBaseUrl || '(relative)');
        
        this.services = [];
        this.establishmentId = null;
        this.init();
    }

    init() {
        this.establishmentId = this.getEstablishmentId();
        if (!this.establishmentId) {
            this.showError('ID do estabelecimento não encontrado na sessão');
            return;
        }
        
        console.log('Establishment ID:', this.establishmentId);
        this.loadServices();
        this.setupEventListeners();
    }

    getEstablishmentId() {
        const session = window.establishmentSession?.getSession();
        return session?.establishmentId || session?.id;
    }

    setupEventListeners() {
        const saveBtn = document.querySelector('#newServiceModal .establishment-btn-success');
        if (saveBtn) {
            saveBtn.addEventListener('click', () => this.saveService());
        }

        const modal = document.getElementById('newServiceModal');
        if (modal) {
            modal.addEventListener('hidden.bs.modal', () => this.clearForm());
        }

        // File upload preview
        const fileInput = document.getElementById('serviceImageFile');
        if (fileInput) {
            fileInput.addEventListener('change', (e) => this.handleFileSelect(e));
        }

        const removeBtn = document.getElementById('removeServiceImage');
        if (removeBtn) {
            removeBtn.addEventListener('click', () => this.removeImagePreview());
        }
    }

    async loadServices() {
        const tableBody = document.getElementById('servicesTableBody');
        if (!tableBody) return;

        try {
            const url = `${this.apiBaseUrl}/api/establishment/services?establishmentId=${this.establishmentId}`;
            console.log('Loading services from:', url);
            
            const response = await fetch(url);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('Server returned error:', response.status, errorText);
                this.showError(`Erro do servidor: ${response.status}`);
                return;
            }
            
            const data = await response.json();

            if (data.success && data.data) {
                this.services = data.data;
                this.renderServices();
            } else {
                this.showError(data.message || 'Erro ao carregar serviços');
            }
        } catch (error) {
            console.error('Error loading services:', error);
            this.showError('Erro ao conectar com o servidor. Verifique sua conexão.');
        }
    }

    renderServices() {
        const tableBody = document.getElementById('servicesTableBody');
        if (!tableBody) return;

        if (this.services.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center py-5">
                        <i class="fas fa-cut fa-3x text-muted mb-3"></i>
                        <h5>Nenhum serviço cadastrado</h5>
                        <p class="text-muted">Clique em "Novo Serviço" para adicionar o primeiro serviço</p>
                    </td>
                </tr>
            `;
            return;
        }

        tableBody.innerHTML = this.services.map(service => this.createServiceRow(service)).join('');
        this.attachRowEventListeners();
    }

    createServiceRow(service) {
        const statusBadge = service.status === 'ACTIVE' ? 
            '<span class="badge bg-success">Ativo</span>' : 
            '<span class="badge bg-secondary">Inativo</span>';

        const duration = this.formatDuration(service.durationMinutes);
        const price = this.formatPrice(service.price);

        // Build image HTML - show image if available, otherwise show icon
        let imageHtml;
        if (service.imageUrl) {
            imageHtml = `<img src="${this.escapeHtml(service.imageUrl)}" alt="${this.escapeHtml(service.name)}" style="width: 50px; height: 50px; object-fit: cover; border-radius: 8px; margin-right: 10px;">`;
        } else {
            imageHtml = `<i class="fas fa-cut text-muted" style="font-size: 1.5rem; margin-right: 10px; width: 50px; text-align: center;"></i>`;
        }

        return `
            <tr>
                <td>
                    <div class="d-flex align-items-center">
                        ${imageHtml}
                        <div>
                            <strong>${this.escapeHtml(service.name)}</strong>
                            ${service.description ? `<br><small class="text-muted">${this.escapeHtml(service.description)}</small>` : ''}
                        </div>
                    </div>
                </td>
                <td>${duration}</td>
                <td><strong>${price}</strong></td>
                <td>${statusBadge}</td>
                <td>
                    <div class="btn-group" role="group">
                        <button class="btn btn-sm btn-primary edit-service" data-id="${service.id}" title="Editar serviço">
                            <i class="fas fa-edit"></i> Editar
                        </button>
                        <button class="btn btn-sm btn-danger delete-service" data-id="${service.id}" title="Excluir serviço">
                            <i class="fas fa-trash"></i> Excluir
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }

    formatDuration(minutes) {
        if (!minutes) return '-';
        
        if (minutes < 60) {
            return `${minutes} min`;
        } else {
            const hours = Math.floor(minutes / 60);
            const mins = minutes % 60;
            if (mins === 0) {
                return `${hours}h`;
            } else {
                return `${hours}h ${mins}min`;
            }
        }
    }

    formatPrice(price) {
        if (price === null || price === undefined) return '-';
        return new Intl.NumberFormat('pt-BR', { 
            style: 'currency', 
            currency: 'BRL' 
        }).format(price);
    }

    attachRowEventListeners() {
        document.querySelectorAll('.edit-service').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = e.currentTarget.getAttribute('data-id');
                this.editService(id);
            });
        });

        document.querySelectorAll('.delete-service').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = e.currentTarget.getAttribute('data-id');
                this.deleteService(id);
            });
        });
    }

    async saveService() {
        const id = document.getElementById('serviceId')?.value?.trim();
        const name = document.getElementById('serviceName')?.value?.trim();
        const description = document.getElementById('serviceDescription')?.value?.trim();
        const durationMinutes = document.getElementById('serviceDuration')?.value;
        const price = document.getElementById('servicePrice')?.value;
        const imageFile = document.getElementById('serviceImageFile')?.files[0];

        if (!name) {
            this.showError('Nome do serviço é obrigatório');
            return;
        }

        if (!durationMinutes || durationMinutes <= 0) {
            this.showError('Duração deve ser maior que zero');
            return;
        }

        if (price === '' || price < 0) {
            this.showError('Preço não pode ser negativo');
            return;
        }

        const service = {
            name,
            description: description || null,
            durationMinutes: parseInt(durationMinutes),
            price: parseFloat(price),
            establishmentId: this.establishmentId
        };

        try {
            let response;
            let url;
            
            if (id) {
                // Update existing service
                url = `${this.apiBaseUrl}/api/establishment/services/${id}?establishmentId=${this.establishmentId}`;
                console.log('Updating service at:', url);
                response = await fetch(url, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(service)
                });
            } else {
                // Create new service
                url = `${this.apiBaseUrl}/api/establishment/services`;
                console.log('Creating service at:', url, 'with data:', service);
                response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(service)
                });
            }

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Server returned error:', response.status, errorText);
                try {
                    const errorData = JSON.parse(errorText);
                    this.showError(errorData.message || `Erro do servidor: ${response.status}`);
                } catch (e) {
                    this.showError(`Erro do servidor: ${response.status}`);
                }
                return;
            }

            const data = await response.json();

            if (data.success) {
                const serviceId = data.data?.id;
                
                // Upload image file if provided
                if (imageFile && serviceId) {
                    await this.uploadServiceImageFile(serviceId, imageFile);
                }
                
                this.showSuccess(id ? 'Serviço atualizado com sucesso!' : 'Serviço cadastrado com sucesso!');
                this.closeModal();
                await this.loadServices();
            } else {
                this.showError(data.message || 'Erro ao salvar serviço');
            }
        } catch (error) {
            console.error('Error saving service:', error);
            this.showError('Erro ao conectar com o servidor. Verifique sua conexão.');
        }
    }

    async uploadServiceImageFile(serviceId, file) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('establishmentId', this.establishmentId);

        try {
            const response = await fetch(`${this.apiBaseUrl}/api/files/service/${serviceId}/upload`, {
                method: 'POST',
                body: formData
            });

            const data = await response.json();
            if (!data.success) {
                this.showError('Erro ao enviar imagem: ' + (data.message || 'Erro desconhecido'));
            }
        } catch (error) {
            console.error('Error uploading service image:', error);
            this.showError('Erro ao enviar imagem');
        }
    }

    async editService(id) {
        const service = this.services.find(s => s.id == id);
        if (!service) {
            this.showError('Serviço não encontrado');
            return;
        }

        // Populate form with service data
        document.getElementById('serviceId').value = service.id;
        document.getElementById('serviceName').value = service.name || '';
        document.getElementById('serviceDescription').value = service.description || '';
        document.getElementById('serviceDuration').value = service.durationMinutes || '';
        document.getElementById('servicePrice').value = service.price || '';

        // Show image preview if service has an image URL
        if (service.imageUrl) {
            const preview = document.getElementById('serviceImagePreview');
            const img = document.getElementById('servicePreviewImg');
            if (preview && img) {
                img.src = service.imageUrl;
                preview.style.display = 'block';
            }
        }

        // Update modal title
        document.getElementById('serviceModalTitle').textContent = 'Editar Serviço';
        
        // Show modal
        const modal = new bootstrap.Modal(document.getElementById('newServiceModal'));
        modal.show();
    }

    async deleteService(id) {
        if (!confirm('Tem certeza que deseja excluir este serviço?')) {
            return;
        }

        try {
            const url = `${this.apiBaseUrl}/api/establishment/services/${id}?establishmentId=${this.establishmentId}`;
            console.log('Deleting service at:', url);
            
            const response = await fetch(url, {
                method: 'DELETE'
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error('Server returned error:', response.status, errorText);
                try {
                    const errorData = JSON.parse(errorText);
                    this.showError(errorData.message || `Erro do servidor: ${response.status}`);
                } catch (e) {
                    this.showError(`Erro do servidor: ${response.status}`);
                }
                return;
            }

            const data = await response.json();

            if (data.success) {
                this.showSuccess('Serviço excluído com sucesso!');
                await this.loadServices();
            } else {
                this.showError(data.message || 'Erro ao excluir serviço');
            }
        } catch (error) {
            console.error('Error deleting service:', error);
            this.showError('Erro ao conectar com o servidor. Verifique sua conexão.');
        }
    }

    clearForm() {
        const fields = ['serviceId', 'serviceName', 'serviceDescription', 'serviceDuration', 'servicePrice'];
        fields.forEach(id => {
            const field = document.getElementById(id);
            if (field) field.value = '';
        });
        
        // Clear file input
        const fileInput = document.getElementById('serviceImageFile');
        if (fileInput) fileInput.value = '';
        
        // Hide preview
        this.removeImagePreview();
        
        // Reset modal title
        const modalTitle = document.getElementById('serviceModalTitle');
        if (modalTitle) {
            modalTitle.textContent = 'Adicionar Novo Serviço';
        }
    }

    handleFileSelect(event) {
        const file = event.target.files[0];
        if (file) {
            // Validate file type
            const validTypes = ['image/jpeg', 'image/png'];
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
                const preview = document.getElementById('serviceImagePreview');
                const img = document.getElementById('servicePreviewImg');
                if (preview && img) {
                    img.src = e.target.result;
                    preview.style.display = 'block';
                }
            };
            reader.readAsDataURL(file);
        }
    }

    removeImagePreview() {
        const preview = document.getElementById('serviceImagePreview');
        const img = document.getElementById('servicePreviewImg');
        const fileInput = document.getElementById('serviceImageFile');
        
        if (preview) preview.style.display = 'none';
        if (img) img.src = '';
        if (fileInput) fileInput.value = '';
    }

    closeModal() {
        const modal = bootstrap.Modal.getInstance(document.getElementById('newServiceModal'));
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
    window.servicesManager = new EstablishmentServicesManager();
});
