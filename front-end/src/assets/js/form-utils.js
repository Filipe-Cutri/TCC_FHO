/**
 * Form Utilities - Common form handling, validation, and progress tracking
 * Shared functionality for form validation, progress tracking, and modal handling
 */

/**
 * Form validation utility
 */
class FormValidator {
    /**
     * Validate email format
     * @param {string} email - Email to validate
     * @returns {boolean} Is valid email
     */
    static isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    /**
     * Validate required field
     * @param {string} value - Value to validate
     * @returns {boolean} Is field filled
     */
    static isRequired(value) {
        return value && value.trim().length > 0;
    }

    /**
     * Validate minimum length
     * @param {string} value - Value to validate
     * @param {number} minLength - Minimum length required
     * @returns {boolean} Meets minimum length
     */
    static minLength(value, minLength) {
        return value && value.trim().length >= minLength;
    }

    /**
     * Add real-time validation to form field
     * @param {HTMLElement} field - Form field element
     * @param {Function} validator - Validation function
     * @param {string} errorMessage - Error message to display
     */
    static addRealTimeValidation(field, validator, errorMessage) {
        field.addEventListener('blur', function() {
            if (!validator(this.value)) {
                this.classList.add('is-invalid');
                this.classList.remove('is-valid');
                
                // Add or update error message
                let errorElement = this.parentElement.querySelector('.invalid-feedback');
                if (!errorElement) {
                    errorElement = document.createElement('div');
                    errorElement.className = 'invalid-feedback';
                    this.parentElement.appendChild(errorElement);
                }
                errorElement.textContent = errorMessage;
            } else {
                this.classList.remove('is-invalid');
                this.classList.add('is-valid');
                
                // Remove error message
                const errorElement = this.parentElement.querySelector('.invalid-feedback');
                if (errorElement) {
                    errorElement.remove();
                }
            }
        });
    }
}

/**
 * Progress tracking utility
 */
class ProgressTracker {
    /**
     * Initialize progress tracker for a form
     * @param {string} formSelector - CSS selector for the form
     * @param {string} progressBarSelector - CSS selector for progress bar
     * @param {string} progressTextSelector - CSS selector for progress text
     * @param {Object} options - Configuration options
     */
    constructor(formSelector, progressBarSelector, progressTextSelector, options = {}) {
        this.form = document.querySelector(formSelector);
        this.progressBar = document.querySelector(progressBarSelector);
        this.progressText = document.querySelector(progressTextSelector);
        
        this.options = {
            checkboxWeight: 1,
            radioWeight: 1,
            textWeight: 1,
            selectWeight: 1,
            ...options
        };
        
        if (this.form) {
            this.setupListeners();
            this.updateProgress();
        }
    }

    /**
     * Setup event listeners for form fields
     */
    setupListeners() {
        const fields = this.form.querySelectorAll('input, textarea, select');
        fields.forEach(field => {
            field.addEventListener('change', () => this.updateProgress());
            field.addEventListener('input', () => this.updateProgress());
        });
    }

    /**
     * Update progress based on filled fields
     */
    updateProgress() {
        let totalSections = 0;
        let filledSections = 0;

        // Count checkboxes (grouped by name)
        const checkboxGroups = {};
        this.form.querySelectorAll('input[type="checkbox"]').forEach(cb => {
            if (!checkboxGroups[cb.name]) {
                checkboxGroups[cb.name] = { total: 0, checked: 0 };
                totalSections += this.options.checkboxWeight;
            }
            checkboxGroups[cb.name].total++;
            if (cb.checked) {
                checkboxGroups[cb.name].checked++;
            }
        });

        // Add filled checkbox groups to progress
        Object.values(checkboxGroups).forEach(group => {
            if (group.checked > 0) {
                filledSections += this.options.checkboxWeight;
            }
        });

        // Count radio button groups
        const radioGroups = {};
        this.form.querySelectorAll('input[type="radio"]').forEach(radio => {
            if (!radioGroups[radio.name]) {
                radioGroups[radio.name] = false;
                totalSections += this.options.radioWeight;
            }
            if (radio.checked) {
                radioGroups[radio.name] = true;
            }
        });

        // Add filled radio groups to progress
        Object.values(radioGroups).forEach(checked => {
            if (checked) {
                filledSections += this.options.radioWeight;
            }
        });

        // Count text inputs and textareas
        this.form.querySelectorAll('input[type="text"], input[type="email"], input[type="password"], textarea').forEach(field => {
            totalSections += this.options.textWeight;
            if (field.value.trim()) {
                filledSections += this.options.textWeight;
            }
        });

        // Count select elements
        this.form.querySelectorAll('select').forEach(select => {
            totalSections += this.options.selectWeight;
            if (select.value && select.value !== '') {
                filledSections += this.options.selectWeight;
            }
        });

        // Calculate percentage
        const percentage = totalSections > 0 ? Math.round((filledSections / totalSections) * 100) : 0;
        
        // Update progress bar and text
        if (this.progressBar) {
            this.progressBar.style.width = percentage + '%';
        }
        if (this.progressText) {
            this.progressText.textContent = percentage + '%';
        }

        return percentage;
    }

    /**
     * Get current progress percentage
     * @returns {number} Current progress percentage
     */
    getProgress() {
        return this.updateProgress();
    }
}

/**
 * Modal utility
 */
class ModalManager {
    /**
     * Show a modal
     * @param {string} modalSelector - CSS selector for modal
     */
    static show(modalSelector) {
        const modalElement = document.querySelector(modalSelector);
        if (modalElement) {
            const modal = new bootstrap.Modal(modalElement);
            modal.show();
            return modal;
        }
    }

    /**
     * Hide a modal
     * @param {string} modalSelector - CSS selector for modal
     */
    static hide(modalSelector) {
        const modalElement = document.querySelector(modalSelector);
        if (modalElement) {
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) {
                modal.hide();
            }
        }
    }

    /**
     * Setup modal form with validation and submission
     * @param {string} modalSelector - CSS selector for modal
     * @param {string} formSelector - CSS selector for form within modal
     * @param {Function} onSubmit - Callback function for form submission
     * @param {Object} options - Configuration options
     */
    static setupFormModal(modalSelector, formSelector, onSubmit, options = {}) {
        const modal = document.querySelector(modalSelector);
        const form = modal?.querySelector(formSelector);
        
        if (!form) return;

        const defaultOptions = {
            showSuccessMessage: true,
            successMessage: 'Dados salvos com sucesso!',
            resetFormOnSuccess: true,
            closeOnSuccess: true,
            ...options
        };

        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const submitBtn = form.querySelector('button[type="submit"]');
            let resetButton = null;
            
            if (submitBtn) {
                resetButton = LoadingManager.setButtonLoading(submitBtn, 'Salvando...');
            }

            try {
                const formData = new FormData(form);
                const data = Object.fromEntries(formData.entries());
                
                // Call the provided submit handler
                await onSubmit(data, form);
                
                if (defaultOptions.showSuccessMessage) {
                    ToastManager.showSuccess(defaultOptions.successMessage);
                }
                
                if (defaultOptions.resetFormOnSuccess) {
                    form.reset();
                }
                
                if (defaultOptions.closeOnSuccess) {
                    ModalManager.hide(modalSelector);
                }
                
                if (submitBtn && resetButton) {
                    LoadingManager.setButtonSuccess(submitBtn, 'Salvo!');
                }
                
            } catch (error) {
                console.error('Form submission error:', error);
                ToastManager.showError('Erro ao salvar os dados. Tente novamente.');
                
                if (resetButton) {
                    resetButton();
                }
            }
        });
    }
}

/**
 * Form data collection utility
 */
class FormDataCollector {
    /**
     * Collect all form data including checkboxes and radio buttons
     * @param {HTMLFormElement} form - Form element
     * @returns {Object} Collected form data
     */
    static collectFormData(form) {
        const data = {};

        // Regular form fields
        const formData = new FormData(form);
        for (let [key, value] of formData.entries()) {
            if (data[key]) {
                // Handle multiple values (like checkboxes with same name)
                if (Array.isArray(data[key])) {
                    data[key].push(value);
                } else {
                    data[key] = [data[key], value];
                }
            } else {
                data[key] = value;
            }
        }

        // Collect checked checkboxes separately
        const checkboxes = form.querySelectorAll('input[type="checkbox"]:checked');
        const checkboxData = {};
        checkboxes.forEach(cb => {
            const groupName = cb.name || 'checkboxes';
            if (!checkboxData[groupName]) {
                checkboxData[groupName] = [];
            }
            checkboxData[groupName].push({
                value: cb.value,
                text: cb.nextElementSibling?.textContent || cb.value
            });
        });

        // Collect selected radio buttons
        const radioButtons = form.querySelectorAll('input[type="radio"]:checked');
        const radioData = {};
        radioButtons.forEach(radio => {
            radioData[radio.name] = {
                value: radio.value,
                text: radio.nextElementSibling?.textContent || radio.value
            };
        });

        return {
            formData: data,
            checkboxes: checkboxData,
            radioButtons: radioData
        };
    }

    /**
     * Get selected items with text content
     * @param {HTMLFormElement} form - Form element
     * @param {string} selector - CSS selector for elements
     * @returns {Array} Array of selected items with text
     */
    static getSelectedItems(form, selector) {
        const items = [];
        form.querySelectorAll(selector).forEach(element => {
            if (element.type === 'checkbox' || element.type === 'radio') {
                if (element.checked) {
                    items.push({
                        value: element.value,
                        text: element.nextElementSibling?.textContent?.trim() || element.value
                    });
                }
            }
        });
        return items;
    }
}

/**
 * Form field filtering utility
 */
class FieldFilter {
    /**
     * Filter form fields based on other field selections
     * @param {string} triggerSelector - CSS selector for trigger field
     * @param {Function} filterFunction - Function to determine which fields to show/hide
     */
    static setupConditionalFields(triggerSelector, filterFunction) {
        document.querySelectorAll(triggerSelector).forEach(trigger => {
            trigger.addEventListener('change', function() {
                filterFunction(this);
            });
        });
    }

    /**
     * Show/hide elements based on selection
     * @param {Array} elements - Elements to show/hide
     * @param {boolean} show - Whether to show or hide
     */
    static toggleElements(elements, show) {
        elements.forEach(element => {
            if (show) {
                element.style.display = '';
                element.removeAttribute('hidden');
            } else {
                element.style.display = 'none';
                element.setAttribute('hidden', 'true');
            }
        });
    }
}

// Export classes globally
window.FormValidator = FormValidator;
window.ProgressTracker = ProgressTracker;
window.ModalManager = ModalManager;
window.FormDataCollector = FormDataCollector;
window.FieldFilter = FieldFilter;

// Export for module usage if needed
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { 
        FormValidator, 
        ProgressTracker, 
        ModalManager, 
        FormDataCollector, 
        FieldFilter 
    };
}