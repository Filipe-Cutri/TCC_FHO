/**
 * Template Manager - Handles loading and inserting reusable HTML templates
 */
class TemplateManager {
    /**
     * Load and insert header template based on page type
     * @param {string} type - 'client' or 'establishment'
     * @param {string} activePage - The current page identifier
     */
    static async loadHeader(type, activePage = '') {
        try {
            const response = await fetch(`../../assets/templates/header-${type}.html`);
            const headerHTML = await response.text();
            
            // Insert header at the beginning of body
            document.body.insertAdjacentHTML('afterbegin', headerHTML);
            
            // Set active navigation item
            if (activePage) {
                this.setActiveNavItem(activePage);
            }
            
            return true;
        } catch (error) {
            console.error('Error loading header template:', error);
            return false;
        }
    }

    /**
     * Load and insert simplified footer
     */
    static async loadFooter() {
        try {
            const response = await fetch('../../assets/templates/footer-simplified.html');
            const footerHTML = await response.text();
            
            // Insert footer at the end of body
            document.body.insertAdjacentHTML('beforeend', footerHTML);
            
            return true;
        } catch (error) {
            console.error('Error loading footer template:', error);
            return false;
        }
    }

    /**
     * Set active navigation item
     * @param {string} activePage - The page identifier
     */
    static setActiveNavItem(activePage) {
        // Remove existing active classes
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.remove('active');
        });

        // Add active class to current page
        const activeLink = document.querySelector(`[data-page="${activePage}"]`);
        if (activeLink) {
            activeLink.classList.add('active');
        }
    }

    /**
     * Initialize templates for a page
     * @param {string} type - 'client' or 'establishment'
     * @param {string} activePage - The current page identifier
     */
    static async initPage(type, activePage) {
        await this.loadHeader(type, activePage);
        await this.loadFooter();
    }
}

// Auto-initialization based on body class
document.addEventListener('DOMContentLoaded', function() {
    const bodyClasses = document.body.classList;
    let pageType = null;
    let activePage = null;

    // Determine page type from body class
    if (bodyClasses.contains('client-page')) {
        pageType = 'client';
    } else if (bodyClasses.contains('establishment-page')) {
        pageType = 'establishment';
    }

    // Determine active page from data attribute or URL
    activePage = document.body.dataset.page || 
                document.querySelector('[data-current-page]')?.dataset.currentPage ||
                'dashboard'; // default

    // Initialize templates if page type is detected
    if (pageType) {
        TemplateManager.initPage(pageType, activePage);
    }
});