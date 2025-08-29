/**
 * Slotfy Establishment Session Manager
 * Manages user sessions and access control for establishment users
 */
class EstablishmentSessionManager {
    constructor() {
        this.sessionKey = 'slotfy_establishment_session';
        this.apiBaseUrl = '/api/establishment';
    }

    /**
     * Set user session
     */
    setSession(userData) {
        const sessionData = {
            user: userData,
            timestamp: new Date().getTime(),
            expires: new Date().getTime() + (24 * 60 * 60 * 1000) // 24 hours
        };
        localStorage.setItem(this.sessionKey, JSON.stringify(sessionData));
    }

    /**
     * Get current session
     */
    getSession() {
        try {
            const sessionData = JSON.parse(localStorage.getItem(this.sessionKey));
            if (!sessionData) return null;

            // Check if session expired
            if (new Date().getTime() > sessionData.expires) {
                this.clearSession();
                return null;
            }

            return sessionData.user;
        } catch (error) {
            console.error('Error reading session:', error);
            this.clearSession();
            return null;
        }
    }

    /**
     * Clear session
     */
    clearSession() {
        localStorage.removeItem(this.sessionKey);
    }

    /**
     * Check if user is logged in
     */
    isLoggedIn() {
        return this.getSession() !== null;
    }

    /**
     * Check if current user is admin
     */
    isAdmin() {
        const session = this.getSession();
        return session && session.role === 'admin';
    }

    /**
     * Check if current user is staff
     */
    isStaff() {
        const session = this.getSession();
        return session && session.role === 'staff';
    }

    /**
     * Get user role
     */
    getUserRole() {
        const session = this.getSession();
        return session ? session.role : null;
    }

    /**
     * Get user name
     */
    getUserName() {
        const session = this.getSession();
        return session ? session.name : null;
    }

    /**
     * Redirect to login if not authenticated
     */
    requireAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = 'establishment-login-enhanced.html';
            return false;
        }
        return true;
    }

    /**
     * Require admin role
     */
    requireAdmin() {
        if (!this.requireAuth()) return false;
        
        if (!this.isAdmin()) {
            alert('Acesso negado. Esta função requer permissões de administrador.');
            window.location.href = 'establishment-dashboard.html';
            return false;
        }
        return true;
    }

    /**
     * Get allowed navigation items based on role
     */
    getAllowedNavigation() {
        const role = this.getUserRole();
        
        const commonItems = [
            { id: 'dashboard', label: 'Dashboard', icon: 'fas fa-tachometer-alt', href: 'establishment-dashboard.html' },
            { id: 'appointments', label: 'Agendamentos', icon: 'fas fa-calendar', href: 'establishment-appointments.html' },
            { id: 'professionals', label: 'Profissionais', icon: 'fas fa-users', href: 'establishment-professionals.html' },
            { id: 'services', label: 'Serviços', icon: 'fas fa-cut', href: 'establishment-services.html' }
        ];

        const adminOnlyItems = [
            { id: 'reports', label: 'Relatórios', icon: 'fas fa-chart-bar', href: 'establishment-reports.html' },
            { id: 'payments', label: 'Pagamentos', icon: 'fas fa-credit-card', href: 'establishment-payments.html' }
        ];

        if (role === 'admin') {
            return [...commonItems, ...adminOnlyItems];
        } else {
            return commonItems;
        }
    }

    /**
     * Update navigation based on user role
     */
    updateNavigation() {
        const navItems = document.querySelector('.navbar-nav.me-auto');
        if (!navItems) return;

        const allowedItems = this.getAllowedNavigation();
        const currentPage = window.location.pathname.split('/').pop();

        // Clear existing navigation
        navItems.innerHTML = '';

        // Add allowed navigation items
        allowedItems.forEach(item => {
            const li = document.createElement('li');
            li.className = 'nav-item';
            
            const isActive = currentPage === item.href ? 'active' : '';
            
            li.innerHTML = `
                <a class="nav-link ${isActive}" href="${item.href}">
                    <i class="${item.icon} me-1"></i>${item.label}
                </a>
            `;
            
            navItems.appendChild(li);
        });

        // Update admin button visibility
        const adminButton = document.querySelector('a[href="establishment-admin.html"]');
        if (adminButton) {
            if (this.isAdmin()) {
                adminButton.style.display = '';
            } else {
                adminButton.style.display = 'none';
            }
        }

        // Update user info in navigation
        this.updateUserInfo();
    }

    /**
     * Update user info display
     */
    updateUserInfo() {
        const session = this.getSession();
        if (!session) return;

        // Update user name display if element exists
        const userNameElements = document.querySelectorAll('.user-name, .establishment-user-name');
        userNameElements.forEach(element => {
            element.textContent = session.name;
        });

        // Update role display if element exists
        const roleElements = document.querySelectorAll('.user-role, .establishment-user-role');
        roleElements.forEach(element => {
            element.textContent = session.roleDescription || session.role;
        });
    }

    /**
     * Logout user
     */
    logout() {
        this.clearSession();
        window.location.href = 'establishment-login-enhanced.html';
    }

    /**
     * Check page access permission
     */
    checkPageAccess(requiredRole = null) {
        if (!this.requireAuth()) return false;

        const currentPage = window.location.pathname.split('/').pop();
        const adminOnlyPages = ['establishment-admin.html', 'establishment-reports.html', 'establishment-payments.html'];

        if (adminOnlyPages.includes(currentPage) && !this.isAdmin()) {
            alert('Acesso negado. Esta página requer permissões de administrador.');
            window.location.href = 'establishment-dashboard.html';
            return false;
        }

        if (requiredRole && this.getUserRole() !== requiredRole) {
            alert(`Acesso negado. Esta função requer permissões de ${requiredRole}.`);
            return false;
        }

        return true;
    }
}

// Create global instance
window.establishmentSession = new EstablishmentSessionManager();

// Auto-initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Check page access
    window.establishmentSession.checkPageAccess();
    
    // Update navigation
    window.establishmentSession.updateNavigation();
    
    // Add logout handlers
    document.querySelectorAll('a[href="establishment-login-enhanced.html"]').forEach(link => {
        if (link.textContent.includes('Sair') || link.innerHTML.includes('sign-out')) {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                window.establishmentSession.logout();
            });
        }
    });
});