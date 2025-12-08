/**
 * Slotfy API Configuration
 * Centralized configuration for API endpoints and utilities
 */

// API Configuration
const API_CONFIG = {
    // Base URL for API calls
    // Automatically detects the environment and uses the appropriate backend URL
    baseUrl: (function() {
        const hostname = window.location.hostname;
        const port = window.location.port;
        const protocol = window.location.protocol;
        
        // Production: Railway or other cloud hosting
        // If running on Railway (*.railway.app or custom domain), use environment variable or relative URL
        if (hostname.includes('railway.app') || (hostname !== 'localhost' && hostname !== '127.0.0.1')) {
            // In production, the backend URL should be set via an environment variable
            // This can be injected during build or set in the HTML as a global variable
            if (typeof window.BACKEND_URL !== 'undefined' && window.BACKEND_URL) {
                return window.BACKEND_URL;
            }
            // If no BACKEND_URL is set, assume same-origin (frontend and backend on same domain)
            return '';
        }
        
        // Local development: Running on localhost
        // If on backend port (8443), use relative URLs
        if (port === '8443') {
            return '';
        }
        
        // If on a different port (e.g., 3000, 5500, 5173), connect to local backend
        // Note: This may fail if the browser blocks self-signed certificates
        return 'https://localhost:8443';
    })(),
    
    // API endpoints
    endpoints: {
        client: {
            login: '/api/client/login',
            register: '/api/client/register',
            forgotPassword: '/api/auth/forgot-password',
            updateEstablishment: '/api/client/establishment',
            establishments: {
                list: '/api/client/establishments',
                details: '/api/client/establishments/{id}',
                services: '/api/client/establishments/{id}/services',
                professionals: '/api/client/establishments/{id}/professionals',
                availability: '/api/client/establishments/{id}/availability'
            },
            appointments: {
                next: '/api/client/appointments/next',
                list: '/api/client/appointments',
                history: '/api/client/appointments/history',
                book: '/api/client/appointments/book',
                details: '/api/client/appointments/{id}',
                cancel: '/api/client/appointments/{id}/cancel'
            },
            profile: {
                get: '/api/client/profile',
                update: '/api/client/profile'
            },
            preferences: {
                get: '/api/client/preferences',
                update: '/api/client/preferences'
            }
        },
        establishment: {
            login: '/api/establishment/login',
            register: '/api/establishment/register',
            registerComplete: '/api/establishment/register-complete',
            forgotPassword: '/api/auth/forgot-password',
            list: '/api/establishment/list',
            createStaff: '/api/establishment/create-staff',
            roles: '/api/establishment/roles',
            clients: '/api/establishment/clients',
            dashboard: {
                overview: '/api/establishment/dashboard/overview',
                todayAppointments: '/api/establishment/dashboard/today-appointments',
                quickActions: '/api/establishment/dashboard/quick-actions',
                serviceCategories: '/api/establishment/dashboard/service-categories',
                professionalPerformance: '/api/establishment/dashboard/professional-performance'
            },
            appointments: {
                list: '/api/establishment/appointments',
                upcoming: '/api/establishment/appointments/upcoming',
                statistics: '/api/establishment/appointments/statistics',
                availability: '/api/establishment/appointments/availability'
            },
            services: {
                list: '/api/establishment/services',
                active: '/api/establishment/services/active',
                create: '/api/establishment/services',
                updateStatus: '/api/establishment/services/{id}/status'
            },
            profile: {
                search: '/api/establishment/profile/search',
                categories: '/api/establishment/profile/categories',
                statistics: '/api/establishment/profile/statistics',
                updateSettings: '/api/establishment/profile/{id}/settings'
            }
        }
    }
};

/**
 * API Utility class for making HTTP requests
 */
class ApiClient {
    constructor() {
        this.baseUrl = API_CONFIG.baseUrl;
    }

    /**
     * Get full URL for an endpoint
     */
    getUrl(endpoint) {
        return this.baseUrl + endpoint;
    }

    /**
     * Make a GET request
     */
    async get(endpoint, params = {}) {
        const url = new URL(this.getUrl(endpoint));
        Object.keys(params).forEach(key => {
            if (params[key] !== undefined && params[key] !== null) {
                url.searchParams.append(key, params[key]);
            }
        });

        const response = await fetch(url.toString(), {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        return this.handleResponse(response);
    }

    /**
     * Make a POST request
     */
    async post(endpoint, data = {}) {
        const response = await fetch(this.getUrl(endpoint), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        return this.handleResponse(response);
    }

    /**
     * Make a PUT request
     */
    async put(endpoint, data = {}) {
        const response = await fetch(this.getUrl(endpoint), {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        return this.handleResponse(response);
    }

    /**
     * Make a DELETE request
     */
    async delete(endpoint) {
        const response = await fetch(this.getUrl(endpoint), {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        return this.handleResponse(response);
    }

    /**
     * Handle API response
     */
    async handleResponse(response) {
        try {
            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.message || `HTTP error! status: ${response.status}`);
            }
            
            return data;
        } catch (error) {
            if (error instanceof SyntaxError) {
                throw new Error('Invalid response format from server');
            }
            throw error;
        }
    }

    /**
     * Replace path parameters in endpoint URLs
     */
    replacePathParams(endpoint, params) {
        let url = endpoint;
        Object.keys(params).forEach(key => {
            url = url.replace(`{${key}}`, params[key]);
        });
        return url;
    }
}

// Create global API client instance
window.apiClient = new ApiClient();
window.API_CONFIG = API_CONFIG;

// Export for module usage if needed
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { ApiClient, API_CONFIG };
}