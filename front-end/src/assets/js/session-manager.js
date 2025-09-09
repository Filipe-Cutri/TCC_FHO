/**
 * Base Session Manager for Slotfy
 * Provides common session management functionality
 */
class BaseSessionManager {
    constructor(sessionKey, apiBaseUrl) {
        this.sessionKey = sessionKey;
        this.apiBaseUrl = apiBaseUrl;
        this.sessionDuration = 24 * 60 * 60 * 1000; // 24 hours
    }

    /**
     * Set user session
     */
    setSession(userData) {
        const sessionData = {
            user: userData,
            timestamp: new Date().getTime(),
            expires: new Date().getTime() + this.sessionDuration
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
     * Update session data
     */
    updateSession(newUserData) {
        const currentSession = this.getSession();
        if (currentSession) {
            this.setSession({ ...currentSession, ...newUserData });
        }
    }

    /**
     * Extend session expiration
     */
    extendSession() {
        const sessionData = JSON.parse(localStorage.getItem(this.sessionKey));
        if (sessionData) {
            sessionData.expires = new Date().getTime() + this.sessionDuration;
            localStorage.setItem(this.sessionKey, JSON.stringify(sessionData));
        }
    }

    /**
     * Get user ID from session
     */
    getUserId() {
        const user = this.getSession();
        return user ? user.id : null;
    }

    /**
     * Get user email from session
     */
    getUserEmail() {
        const user = this.getSession();
        return user ? user.email : null;
    }

    /**
     * Get user name from session
     */
    getUserName() {
        const user = this.getSession();
        return user ? user.name : null;
    }

    /**
     * Logout user and redirect
     */
    logout(redirectPath = '../../index.html') {
        this.clearSession();
        window.location.href = redirectPath;
    }

    /**
     * Redirect if not authenticated
     */
    requireAuth(loginPath) {
        if (!this.isLoggedIn()) {
            window.location.href = loginPath;
            return false;
        }
        return true;
    }
}

/**
 * Client Session Manager
 */
class ClientSessionManager extends BaseSessionManager {
    constructor() {
        super('slotfy_client_session', '/api/client');
    }

    /**
     * Require client authentication
     */
    requireAuth() {
        return super.requireAuth('../client/client-login.html');
    }

    /**
     * Logout and redirect to client area
     */
    logout() {
        super.logout('../client/client-login.html');
    }
}

/**
 * Establishment Session Manager
 */
class EstablishmentSessionManager extends BaseSessionManager {
    constructor() {
        super('slotfy_establishment_session', '/api/establishment');
    }

    /**
     * Get user role from session
     */
    getUserRole() {
        const user = this.getSession();
        return user ? user.role : null;
    }

    /**
     * Get establishment ID from session
     */
    getEstablishmentId() {
        const user = this.getSession();
        return user ? user.establishmentId : null;
    }

    /**
     * Check if user has admin role
     */
    isAdmin() {
        return this.getUserRole() === 'admin';
    }

    /**
     * Check if user has staff role
     */
    isStaff() {
        return this.getUserRole() === 'staff';
    }

    /**
     * Require establishment authentication
     */
    requireAuth() {
        return super.requireAuth('../establishment/establishment-login.html');
    }

    /**
     * Require admin authentication
     */
    requireAdmin() {
        if (!this.requireAuth()) return false;
        
        if (!this.isAdmin()) {
            alert('Acesso negado. Apenas administradores podem acessar esta área.');
            window.location.href = 'establishment-dashboard.html';
            return false;
        }
        return true;
    }

    /**
     * Logout and redirect to establishment area
     */
    logout() {
        super.logout('../establishment/establishment-login.html');
    }
}

// Create global instances
const clientSessionManager = new ClientSessionManager();
const establishmentSessionManager = new EstablishmentSessionManager();

// Backward compatibility exports
window.ClientSessionManager = ClientSessionManager;
window.EstablishmentSessionManager = EstablishmentSessionManager;
window.clientSessionManager = clientSessionManager;
window.establishmentSessionManager = establishmentSessionManager;