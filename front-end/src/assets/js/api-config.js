/**
 * Slotfy API Configuration
 * Centralized configuration for API endpoints and utilities
 */

// API Configuration
const API_CONFIG = {
    // Base URL for API calls
    // Automatically detects the environment and uses the appropriate backend URL
    baseUrl: (function() {
        // Usa window.BACKEND_URL do config.js (mais confiável)
        if (typeof window.BACKEND_URL !== 'undefined' && window.BACKEND_URL) {
            console.log('✅ Using BACKEND_URL:', window.BACKEND_URL);
            return window.BACKEND_URL;
        }

        // Fallback para localhost (desenvolvimento)
        const hostname = window.location.hostname;
        if (hostname === 'localhost' || hostname === '127.0.0.1') {
            console.log('🔧 Development mode - using localhost:8443');
            return 'https://localhost:8443';
        }

        // Fallback para produção
        console.warn('⚠️ BACKEND_URL not found! Using fallback.');
        return 'https://api.slotfy.com.br';
    })(),
    
    // API endpoints
    endpoints: {
        client: {
            login: '/api/client/login',
            register: '/api/client/register',
            forgotPassword: '/api/auth/forgot-password',
            updateEstablishment: '/api/client/establishment',
            dashboard: '/api/client/dashboard',
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