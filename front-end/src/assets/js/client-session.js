/**
 * Slotfy Client Session Manager
 * Manages user sessions and access control for client users
 */
class ClientSessionManager {
    constructor() {
        this.sessionKey = 'slotfy_client_session';
        this.apiBaseUrl = '/api/client';
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
     * Get user name
     */
    getUserName() {
        const session = this.getSession();
        return session ? session.name : null;
    }

    /**
     * Get user email
     */
    getUserEmail() {
        const session = this.getSession();
        return session ? session.email : null;
    }

    /**
     * Get user phone
     */
    getUserPhone() {
        const session = this.getSession();
        return session ? session.phone : null;
    }

    /**
     * Get user ID
     */
    getUserId() {
        const session = this.getSession();
        return session ? session.id : null;
    }

    /**
     * Redirect to login if not authenticated
     */
    requireAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = 'client-login.html';
            return false;
        }
        return true;
    }

    /**
     * Update user info display
     */
    updateUserInfo() {
        const session = this.getSession();
        if (!session) return;

        // Update user name display if element exists
        const userNameElements = document.querySelectorAll('.user-name, .client-user-name');
        userNameElements.forEach(element => {
            element.textContent = session.name;
        });

        // Update email display if element exists
        const emailElements = document.querySelectorAll('.user-email, .client-user-email');
        emailElements.forEach(element => {
            element.textContent = session.email;
        });

        // Update phone display if element exists
        const phoneElements = document.querySelectorAll('.user-phone, .client-user-phone');
        phoneElements.forEach(element => {
            element.textContent = session.phone || '';
        });
    }

    /**
     * Logout user
     */
    logout() {
        this.clearSession();
        window.location.href = 'client-login.html';
    }

    /**
     * Update navigation for logged in client
     */
    updateNavigation() {
        const session = this.getSession();
        if (!session) return;

        // Update user greeting if element exists
        const greetingElements = document.querySelectorAll('.user-greeting');
        greetingElements.forEach(element => {
            element.textContent = `Olá, ${session.name}!`;
        });

        // Show/hide login/logout buttons
        const loginButtons = document.querySelectorAll('a[href="client-login.html"]:not(.logout-btn)');
        const logoutButtons = document.querySelectorAll('.logout-btn');

        loginButtons.forEach(btn => {
            if (!btn.classList.contains('logout-btn')) {
                btn.style.display = 'none';
            }
        });

        logoutButtons.forEach(btn => {
            btn.style.display = '';
        });
    }

    /**
     * Check if on login/register page and redirect if already logged in
     */
    redirectIfLoggedIn() {
        if (this.isLoggedIn()) {
            const currentPage = window.location.pathname.split('/').pop();
            if (currentPage === 'client-login.html' || currentPage === 'client-register.html') {
                window.location.href = 'client-dashboard.html';
            }
        }
    }
}

// Create global instance
window.clientSession = new ClientSessionManager();

// Auto-initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Update user info display
    window.clientSession.updateUserInfo();
    
    // Update navigation
    window.clientSession.updateNavigation();
    
    // Redirect if already logged in on auth pages
    window.clientSession.redirectIfLoggedIn();
    
    // Add logout handlers for client logout buttons
    document.querySelectorAll('.logout-btn, a[href="client-login.html"]').forEach(link => {
        if (link.textContent.includes('Sair') || link.innerHTML.includes('sign-out') || link.classList.contains('logout-btn')) {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                window.clientSession.logout();
            });
        }
    });
});