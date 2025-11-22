// API Client for AutoEscalate Ops backend

const API_BASE_URL = '/api/issues';

// Helper function for making API requests with error handling
async function apiRequest(url, options = {}) {
    try {
        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            ...options,
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API request failed:', error);
        throw error;
    }
}

// GET /api/issues - Fetch all issues with optional filters
export async function getIssues(filters = {}) {
    const params = new URLSearchParams();

    if (filters.status) params.append('status', filters.status);
    if (filters.priority) params.append('priority', filters.priority);

    const queryString = params.toString();
    const url = queryString ? `${API_BASE_URL}?${queryString}` : API_BASE_URL;

    return apiRequest(url);
}

// GET /api/issues/{id} - Fetch single issue by ID
export async function getIssueById(id) {
    return apiRequest(`${API_BASE_URL}/${id}`);
}

// POST /api/issues - Create new issue
export async function createIssue(issueData) {
    return apiRequest(API_BASE_URL, {
        method: 'POST',
        body: JSON.stringify(issueData),
    });
}

// PATCH /api/issues/{id} - Update issue (typically status)
export async function updateIssue(id, updates) {
    return apiRequest(`${API_BASE_URL}/${id}`, {
        method: 'PATCH',
        body: JSON.stringify(updates),
    });
}
