/**
 * Slotfy API Configuration
 * Centralized configuration for API endpoints and utilities
 */

// API Configuration
const API_CONFIG = {
    // Base URL for API calls - Updated to use HTTPS
    baseUrl: window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' 
        ? 'https://localhost:8443' 
        : '', // Use relative URLs in production
    
    // API endpoints
    endpoints: {
        client: {
            login: '/api/client/login',
            register: '/api/client/register',
            forgotPassword: '/api/client/forgot-password',
            appointments: {
                next: '/api/client/appointments/next',
                list: '/api/client/appointments',
                history: '/api/client/appointments/history',
                book: '/api/client/appointments/book'
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
            forgotPassword: '/api/establishment/forgot-password',
            createStaff: '/api/establishment/create-staff',
            roles: '/api/establishment/roles',
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