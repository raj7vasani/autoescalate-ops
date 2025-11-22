// Main application logic for Issue Tracker

import { getIssues, getIssueById, createIssue, updateIssue } from './api.js';

// Application state
let currentView = 'list';
let currentFilters = {};
let currentIssue = null;

// Initialize app on load
document.addEventListener('DOMContentLoaded', () => {
    renderApp();
    loadIssuesList();
});

// Main app render function
function renderApp() {
    const container = document.getElementById('app-container');
    container.innerHTML = `
        <nav class="nav-tabs">
            <button class="nav-tab active" data-view="list">Issues List</button>
            <button class="nav-tab" data-view="create">Create Issue</button>
        </nav>

        <div id="list-view" class="view active"></div>
        <div id="detail-view" class="view"></div>
        <div id="create-view" class="view"></div>
    `;

    // Set up navigation
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.addEventListener('click', (e) => {
            const view = e.target.dataset.view;
            switchView(view);
        });
    });

    renderListView();
    renderCreateView();
}

// Switch between views
function switchView(view) {
    currentView = view;

    // Update nav tabs
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.classList.toggle('active', tab.dataset.view === view);
    });

    // Update view visibility
    document.querySelectorAll('.view').forEach(viewEl => {
        viewEl.classList.remove('active');
    });

    if (view === 'list') {
        document.getElementById('list-view').classList.add('active');
        loadIssuesList();
    } else if (view === 'detail') {
        document.getElementById('detail-view').classList.add('active');
    } else if (view === 'create') {
        document.getElementById('create-view').classList.add('active');
    }
}

// Render Issues List View
function renderListView() {
    const listView = document.getElementById('list-view');
    listView.innerHTML = `
        <div class="card">
            <div class="filters">
                <div class="filter-group">
                    <label for="search-title">Search by Title</label>
                    <input type="text" id="search-title" placeholder="Search issues...">
                </div>
                <div class="filter-group">
                    <label for="filter-status">Status</label>
                    <select id="filter-status">
                        <option value="">All Status</option>
                        <option value="New">New</option>
                        <option value="InProgress">In Progress</option>
                        <option value="Resolved">Resolved</option>
                        <option value="Closed">Closed</option>
                    </select>
                </div>
                <div class="filter-group">
                    <label for="filter-priority">Priority</label>
                    <select id="filter-priority">
                        <option value="">All Priority</option>
                        <option value="Low">Low</option>
                        <option value="Medium">Medium</option>
                        <option value="High">High</option>
                        <option value="Critical">Critical</option>
                    </select>
                </div>
            </div>
        </div>

        <div class="card">
            <div id="issues-container">
                <div class="loading">Loading issues...</div>
            </div>
        </div>
    `;

    // Set up filter event listeners
    document.getElementById('filter-status').addEventListener('change', applyFilters);
    document.getElementById('filter-priority').addEventListener('change', applyFilters);
    document.getElementById('search-title').addEventListener('input', applyFilters);
}

// Apply filters and reload issues
function applyFilters() {
    const status = document.getElementById('filter-status').value;
    const priority = document.getElementById('filter-priority').value;
    const searchText = document.getElementById('search-title').value.toLowerCase();

    currentFilters = { status, priority, searchText };
    loadIssuesList();
}

// Load and display issues list
async function loadIssuesList() {
    const container = document.getElementById('issues-container');
    if (!container) return;

    container.innerHTML = '<div class="loading">Loading issues...</div>';

    try {
        // Fetch issues with status/priority filters
        let issues = await getIssues({
            status: currentFilters.status,
            priority: currentFilters.priority,
        });

        // Apply client-side text search filter
        if (currentFilters.searchText) {
            issues = issues.filter(issue =>
                issue.title.toLowerCase().includes(currentFilters.searchText)
            );
        }

        if (issues.length === 0) {
            container.innerHTML = '<div class="empty-state">No issues found</div>';
            return;
        }

        // Render issues table
        container.innerHTML = `
            <table class="issues-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Title</th>
                        <th>Type</th>
                        <th>Priority</th>
                        <th>Status</th>
                        <th>Location</th>
                        <th>Reported At</th>
                    </tr>
                </thead>
                <tbody>
                    ${issues.map(issue => `
                        <tr data-id="${issue.id}">
                            <td>${issue.id.substring(0, 8)}...</td>
                            <td><strong>${escapeHtml(issue.title)}</strong></td>
                            <td>${escapeHtml(issue.type || 'N/A')}</td>
                            <td><span class="badge priority-${(issue.priority || 'low').toLowerCase()}">${escapeHtml(issue.priority || 'N/A')}</span></td>
                            <td><span class="badge status-${(issue.status || 'new').toLowerCase()}">${escapeHtml(issue.status || 'N/A')}</span></td>
                            <td>${escapeHtml(issue.location || 'N/A')}</td>
                            <td>${formatDateTime(issue.reportedAt)}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;

        // Add click handlers to rows
        document.querySelectorAll('.issues-table tbody tr').forEach(row => {
            row.addEventListener('click', () => {
                const id = row.dataset.id;
                showIssueDetail(id);
            });
        });

    } catch (error) {
        container.innerHTML = `<div class="error-message">Failed to load issues: ${error.message}</div>`;
    }
}

// Show issue detail view
async function showIssueDetail(id) {
    const detailView = document.getElementById('detail-view');
    detailView.innerHTML = '<div class="loading">Loading issue details...</div>';

    switchView('detail');

    try {
        currentIssue = await getIssueById(id);
        renderIssueDetail(currentIssue);
    } catch (error) {
        detailView.innerHTML = `<div class="error-message">Failed to load issue: ${error.message}</div>`;
    }
}

// Render issue detail view
function renderIssueDetail(issue) {
    const detailView = document.getElementById('detail-view');
    detailView.innerHTML = `
        <div class="card">
            <div class="actions-bar">
                <button class="btn btn-secondary" onclick="window.app.backToList()">← Back to List</button>
                <div class="btn-group">
                    <select id="status-update" class="btn" style="padding: 0.65rem 1rem;">
                        <option value="">Update Status</option>
                        <option value="New">New</option>
                        <option value="InProgress">In Progress</option>
                        <option value="Resolved">Resolved</option>
                        <option value="Closed">Closed</option>
                    </select>
                </div>
            </div>

            <div class="detail-header">
                <h2>${escapeHtml(issue.title)}</h2>
                <div class="detail-meta">
                    <span><strong>ID:</strong> ${issue.id}</span>
                    <span><strong>Status:</strong> <span class="badge status-${(issue.status || 'new').toLowerCase()}">${escapeHtml(issue.status || 'N/A')}</span></span>
                    <span><strong>Priority:</strong> <span class="badge priority-${(issue.priority || 'low').toLowerCase()}">${escapeHtml(issue.priority || 'N/A')}</span></span>
                </div>
            </div>

            <div class="detail-section">
                <h3>Issue Details</h3>
                <div class="detail-grid">
                    <div class="detail-field">
                        <label>Type</label>
                        <div class="value">${escapeHtml(issue.type || 'N/A')}</div>
                    </div>
                    <div class="detail-field">
                        <label>Location</label>
                        <div class="value">${escapeHtml(issue.location || 'N/A')}</div>
                    </div>
                    <div class="detail-field">
                        <label>Severity</label>
                        <div class="value">${issue.severity || 'N/A'}</div>
                    </div>
                    <div class="detail-field">
                        <label>Reported At</label>
                        <div class="value">${formatDateTime(issue.reportedAt)}</div>
                    </div>
                    <div class="detail-field">
                        <label>Acknowledged At</label>
                        <div class="value">${formatDateTime(issue.acknowledgedAt)}</div>
                    </div>
                    <div class="detail-field">
                        <label>Resolved At</label>
                        <div class="value">${formatDateTime(issue.resolvedAt)}</div>
                    </div>
                </div>
            </div>

            <div class="detail-section">
                <h3>Description</h3>
                <p>${escapeHtml(issue.description || 'No description provided')}</p>
            </div>

            <div class="detail-section">
                <h3>Comments</h3>
                <div class="comments-section">
                    <div class="comment-placeholder">Comments feature will be available soon</div>
                </div>
            </div>
        </div>
    `;

    // Set up status update handler
    document.getElementById('status-update').addEventListener('change', async (e) => {
        const newStatus = e.target.value;
        if (!newStatus) return;

        try {
            await updateIssue(issue.id, { status: newStatus });
            alert('Status updated successfully!');
            showIssueDetail(issue.id); // Reload
        } catch (error) {
            alert('Failed to update status: ' + error.message);
        }
        e.target.value = ''; // Reset dropdown
    });
}

// Render create issue view
function renderCreateView() {
    const createView = document.getElementById('create-view');
    createView.innerHTML = `
        <div class="card">
            <h2>Create New Issue</h2>
            <form id="create-issue-form">
                <div class="form-group">
                    <label for="title" class="required">Title</label>
                    <input type="text" id="title" name="title" required maxlength="100">
                    <div class="error" id="title-error"></div>
                </div>

                <div class="form-group">
                    <label for="description" class="required">Description</label>
                    <textarea id="description" name="description" required maxlength="1000"></textarea>
                    <div class="error" id="description-error"></div>
                </div>

                <div class="form-group">
                    <label for="type" class="required">Type</label>
                    <select id="type" name="type" required>
                        <option value="">Select type...</option>
                        <option value="MACHINE_BREAKDOWN">Machine Breakdown</option>
                        <option value="QUALITY_DEFECT">Quality Defect</option>
                        <option value="MAINTENANCE">Maintenance</option>
                        <option value="SAFETY">Safety</option>
                        <option value="OTHER">Other</option>
                    </select>
                    <div class="error" id="type-error"></div>
                </div>

                <div class="form-group">
                    <label for="priority" class="required">Priority</label>
                    <select id="priority" name="priority" required>
                        <option value="">Select priority...</option>
                        <option value="Low">Low</option>
                        <option value="Medium">Medium</option>
                        <option value="High">High</option>
                        <option value="Critical">Critical</option>
                    </select>
                    <div class="error" id="priority-error"></div>
                </div>

                <div class="form-group">
                    <label for="location" class="required">Location</label>
                    <input type="text" id="location" name="location" required maxlength="100" placeholder="e.g., Plant A / Line 3">
                    <div class="error" id="location-error"></div>
                </div>

                <div class="form-group">
                    <label for="severity" class="required">Severity (1-5)</label>
                    <input type="number" id="severity" name="severity" required min="1" max="5" value="3">
                    <div class="error" id="severity-error"></div>
                </div>

                <div style="display: flex; gap: 1rem; margin-top: 2rem;">
                    <button type="submit" class="btn btn-primary">Create Issue</button>
                    <button type="button" class="btn btn-secondary" onclick="window.app.backToList()">Cancel</button>
                </div>
            </form>
        </div>
    `;

    // Set up form submission handler
    document.getElementById('create-issue-form').addEventListener('submit', handleCreateIssue);
}

// Handle create issue form submission
async function handleCreateIssue(e) {
    e.preventDefault();

    // Clear previous errors
    document.querySelectorAll('.error').forEach(el => el.textContent = '');

    // Get form data
    const formData = new FormData(e.target);
    const issueData = {
        title: formData.get('title').trim(),
        description: formData.get('description').trim(),
        type: formData.get('type'),
        priority: formData.get('priority'),
        location: formData.get('location').trim(),
        severity: parseInt(formData.get('severity')),
    };

    // Basic validation
    let hasErrors = false;

    if (!issueData.title) {
        document.getElementById('title-error').textContent = 'Title is required';
        hasErrors = true;
    }

    if (!issueData.description) {
        document.getElementById('description-error').textContent = 'Description is required';
        hasErrors = true;
    }

    if (!issueData.type) {
        document.getElementById('type-error').textContent = 'Type is required';
        hasErrors = true;
    }

    if (!issueData.priority) {
        document.getElementById('priority-error').textContent = 'Priority is required';
        hasErrors = true;
    }

    if (!issueData.location) {
        document.getElementById('location-error').textContent = 'Location is required';
        hasErrors = true;
    }

    if (issueData.severity < 1 || issueData.severity > 5) {
        document.getElementById('severity-error').textContent = 'Severity must be between 1 and 5';
        hasErrors = true;
    }

    if (hasErrors) return;

    try {
        // Disable submit button during creation
        const submitBtn = e.target.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Creating...';

        await createIssue(issueData);

        alert('Issue created successfully!');
        e.target.reset();
        switchView('list');

    } catch (error) {
        alert('Failed to create issue: ' + error.message);
    } finally {
        const submitBtn = e.target.querySelector('button[type="submit"]');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Create Issue';
    }
}

// Utility function to escape HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Utility function to format date/time
function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
}

// Expose functions to global scope for inline onclick handlers
window.app = {
    backToList: () => switchView('list'),
};
