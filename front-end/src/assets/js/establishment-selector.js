/**
 * Establishment Selector Component
 * Provides a dropdown/modal to select establishments
 */

class EstablishmentSelector {
    constructor() {
        this.establishments = [];
        this.selectedEstablishmentId = null;
        this.onSelectCallback = null;
    }

    /**
     * Load establishments from API
     */
    async loadEstablishments() {
        try {
            const response = await window.apiClient.get(API_CONFIG.endpoints.establishment.list);
            if (response.success) {
                // Filter to ensure only valid establishments are included
                // Backend already filters for ACTIVE status, we just validate required fields
                this.establishments = (response.data || []).filter(est => 
                    est && est.id && est.name
                );
                console.log(`Loaded ${this.establishments.length} active establishments`);
                return this.establishments;
            } else {
                throw new Error(response.message || 'Erro ao carregar estabelecimentos');
            }
        } catch (error) {
            console.error('Error loading establishments:', error);
            throw error;
        }
    }

    /**
     * Create establishment selector dropdown HTML
     */
    createSelectorHTML(includeNone = false, selectedId = null) {
        let html = '<select class="form-control establishment-selector" id="establishmentSelect">';
        
        if (includeNone) {
            html += '<option value="">Selecione um estabelecimento (opcional)</option>';
        } else {
            html += '<option value="">Selecione um estabelecimento</option>';
        }
        
        this.establishments.forEach(est => {
            const selected = est.id == selectedId ? 'selected' : '';
            html += `<option value="${est.id}" ${selected}>${est.name}${est.category ? ' - ' + est.category : ''}</option>`;
        });
        
        html += '</select>';
        return html;
    }

    /**
     * Create establishment card grid HTML for modal
     */
    createCardGridHTML(selectedId = null) {
        if (this.establishments.length === 0) {
            return '<div class="alert alert-info">Nenhum estabelecimento disponível no momento.</div>';
        }

        let html = '<div class="establishment-grid row">';
        
        this.establishments.forEach(est => {
            const selected = est.id == selectedId ? 'selected' : '';
            html += `
                <div class="col-md-6 col-lg-4 mb-3">
                    <div class="establishment-card ${selected}" data-id="${est.id}">
                        ${est.imageUrl ? 
                            `<img src="${est.imageUrl}" alt="${est.name}" class="establishment-image">` : 
                            `<div class="establishment-placeholder"><i class="fas fa-store fa-3x"></i></div>`
                        }
                        <div class="establishment-info">
                            <h5 class="establishment-name">${est.name}</h5>
                            ${est.category ? `<p class="establishment-category"><i class="fas fa-tag me-1"></i>${est.category}</p>` : ''}
                            ${est.address ? `<p class="establishment-address"><i class="fas fa-map-marker-alt me-1"></i>${est.address}</p>` : ''}
                        </div>
                    </div>
                </div>
            `;
        });
        
        html += '</div>';
        return html;
    }

    /**
     * Show establishment selection modal
     */
    async showModal(selectedId = null, onSelect = null) {
        // Load establishments if not already loaded
        if (this.establishments.length === 0) {
            await this.loadEstablishments();
        }

        this.onSelectCallback = onSelect;
        this.selectedEstablishmentId = selectedId;

        // Create modal HTML
        const modalHtml = `
            <div class="modal fade" id="establishmentModal" tabindex="-1" aria-labelledby="establishmentModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-lg modal-dialog-scrollable">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="establishmentModalLabel">
                                <i class="fas fa-store me-2"></i>
                                Selecione um Estabelecimento
                            </h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            ${this.createCardGridHTML(selectedId)}
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-primary" id="confirmEstablishmentBtn" disabled>Confirmar</button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Remove existing modal if any
        const existingModal = document.getElementById('establishmentModal');
        if (existingModal) {
            existingModal.remove();
        }

        // Add modal to body
        document.body.insertAdjacentHTML('beforeend', modalHtml);

        // Initialize modal
        const modalElement = document.getElementById('establishmentModal');
        const modal = new bootstrap.Modal(modalElement);

        // Setup event listeners
        this.setupModalEventListeners(modal);

        // Show modal
        modal.show();

        // Cleanup on close
        modalElement.addEventListener('hidden.bs.modal', () => {
            modalElement.remove();
        });
    }

    /**
     * Setup event listeners for modal
     */
    setupModalEventListeners(modal) {
        const confirmBtn = document.getElementById('confirmEstablishmentBtn');
        
        // Card selection
        document.querySelectorAll('.establishment-card').forEach(card => {
            card.addEventListener('click', () => {
                // Remove selected class from all cards
                document.querySelectorAll('.establishment-card').forEach(c => c.classList.remove('selected'));
                
                // Add selected class to clicked card
                card.classList.add('selected');
                
                // Update selected ID
                this.selectedEstablishmentId = card.dataset.id;
                
                // Enable confirm button
                confirmBtn.disabled = false;
            });
        });

        // Confirm button
        confirmBtn.addEventListener('click', () => {
            if (this.selectedEstablishmentId && this.onSelectCallback) {
                this.onSelectCallback(parseInt(this.selectedEstablishmentId));
            }
            modal.hide();
        });
    }

    /**
     * Get establishment by ID
     */
    getEstablishmentById(id) {
        return this.establishments.find(est => est.id == id);
    }
}

// Export for use
window.EstablishmentSelector = EstablishmentSelector;
