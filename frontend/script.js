// Base URL for the backend API
const API_BASE = 'http://localhost:8080';

// Current user ID (in a real app, this would come from auth/session)
// For simplicity, we'll store it in localStorage or use a fixed one for demo.
// We'll use a fixed user ID for demonstration: 'user1'
// In a real app, you would have a login system and store the user ID or token.
let currentUserId = 'user1';

// Page elements
const homePage = document.getElementById('homePage');
const dashboardPage = document.getElementById('dashboardPage');
const managePage = document.getElementById('managePage');
const homeBtn = document.getElementById('homeBtn');
const dashboardBtn = document.getElementById('dashboardBtn');
const manageBtn = document.getElementById('manageBtn');
const plansContainer = document.getElementById('plansContainer');
const userInfoDiv = document.getElementById('userInfo');
const subscriptionForm = document.getElementById('subscriptionForm');
const formMessageDiv = document.getElementById('formMessage');
const upgradeDowngradeBtn = document.getElementById('upgradeDowngradeBtn');
const cancelSubscriptionBtn = document.getElementById('cancelSubscriptionBtn');

// Plan select element
const planSelect = document.getElementById('planSelect');

// Initialize the app
function init() {
    // Set up event listeners for navigation
    homeBtn.addEventListener('click', showHomePage);
    dashboardBtn.addEventListener('click', showDashboardPage);
    manageBtn.addEventListener('click', showManagePage);

    // Set up form submission
    subscriptionForm.addEventListener('submit', handleSubscribeForm);

    // Set up upgrade/downgrade and cancel buttons
    upgradeDowngradeBtn.addEventListener('click', showUpgradeDowngradePrompt);
    cancelSubscriptionBtn.addEventListener('click', handleCancelSubscription);

    // Load plans and display home page by default
    loadPlans();
    showHomePage();
}

// Show home page
function showHomePage() {
    hideAllPages();
    homePage.style.display = 'block';
    // Update active nav button
    setActiveBtn(homeBtn);
}

// Show dashboard page
function showDashboardPage() {
    hideAllPages();
    dashboardPage.style.display = 'block';
    setActiveBtn(dashboardBtn);
    loadUserDashboard();
}

// Show manage page
function showManagePage() {
    hideAllPages();
    managePage.style.display = 'block';
    setActiveBtn(manageBtn);
    loadPlansIntoSelect(); // Ensure plan select is updated
}

// Hide all pages
function hideAllPages() {
    homePage.style.display = 'none';
    dashboardPage.style.display = 'none';
    managePage.style.display = 'none';
}

// Set active button in nav
function setActiveBtn(activeBtn) {
    [homeBtn, dashboardBtn, manageBtn].forEach(btn => {
        btn.classList.remove('active');
    });
    activeBtn.classList.add('active');
}

// Load plans from backend and display in home page
async function loadPlans() {
    try {
        const response = await fetch(`${API_BASE}/plans`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const plans = await response.json();
        displayPlans(plans);
    } catch (error) {
        console.error('Error loading plans:', error);
        plansContainer.innerHTML = '<p>Error loading plans. Please try again later.</p>';
    }
}

// Display plans in the home page
function displayPlans(plans) {
    plansContainer.innerHTML = ''; // Clear existing content
    plans.forEach(plan => {
        const planCard = document.createElement('div');
        planCard.className = 'plan-card';
        planCard.innerHTML = `
            <h3>${plan.name}</h3>
            <div class="price">$${plan.price} / ${plan.duration}</div>
            <div class="features">
                <p><strong>Features:</strong> ${plan.features}</p>
            </div>
        `;
        plansContainer.appendChild(planCard);
    });
}

// Load plans into the select element in the manage page
async function loadPlansIntoSelect() {
    try {
        const response = await fetch(`${API_BASE}/plans`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const plans = await response.json();
        planSelect.innerHTML = '<option value="">-- Select a Plan --</option>'; // Reset
        plans.forEach(plan => {
            const option = document.createElement('option');
            option.value = plan.name;
            option.textContent = `${plan.name} - $${plan.price}/${plan.duration}`;
            planSelect.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading plans for select:', error);
        planSelect.innerHTML = '<option value="">Error loading plans</option>';
    }
}

// Handle subscription form submission
async function handleSubscribeForm(e) {
    e.preventDefault();
    const userId = document.getElementById('userId').value.trim();
    const name = document.getElementById('name').value.trim();
    const email = document.getElementById('email').value.trim();
    const planName = planSelect.value;

    // Reset message
    formMessageDiv.textContent = '';
    formMessageDiv.className = '';

    // Basic validation
    if (!userId || !name || !email || !planName) {
        showFormMessage('Please fill in all fields', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/subscribe`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: userId,
                name: name,
                email: email,
                planName: planName
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Subscription failed');
        }

        const data = await response.json();
        showFormMessage('Subscription successful!', 'success');
        // Reset form
        subscriptionForm.reset();
        // Optionally, we can set the current user and go to dashboard
        currentUserId = userId;
        // Show dashboard after a short delay
        setTimeout(() => {
            showDashboardPage();
        }, 1500);
    } catch (error) {
        console.error('Error subscribing:', error);
        showFormMessage(error.message || 'Subscription failed. Please try again.', 'error');
    }
}

// Show a message in the form
function showFormMessage(message, type) {
    formMessageDiv.textContent = message;
    formMessageDiv.className = type; // 'success' or 'error'
}

// Load user dashboard (current subscription)
async function loadUserDashboard() {
    try {
        const response = await fetch(`${API_BASE}/user/${currentUserId}`);
        if (!response.ok) {
            if (response.status === 404) {
                userInfoDiv.innerHTML = '<p>No subscription found for this user. Please subscribe first.</p>';
            } else {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return;
        }
        const userData = await response.json();
        displayUserInfo(userData);
    } catch (error) {
        console.error('Error loading user dashboard:', error);
        userInfoDiv.innerHTML = '<p>Error loading dashboard. Please try again later.</p>';
    }
}

// Display user info in the dashboard
function displayUserInfo(userData) {
    userInfoDiv.innerHTML = `
        <p><strong>User ID:</strong> ${userData.userId}</p>
        <p><strong>Name:</strong> ${userData.name}</p>
        <p><strong>Email:</strong> ${userData.email}</p>
        <p><strong>Current Plan:</strong> ${userData.currentPlan}</p>
        <p><strong>Start Date:</strong> ${userData.startDate}</p>
        <p><strong>End Date:</strong> ${userData.endDate}</p>
    `;
}

// Show upgrade/downgrade prompt (simple version using prompt)
function showUpgradeDowngradePrompt() {
    const newPlanName = prompt('Enter the new plan name (Basic, Standard, Premium):');
    if (newPlanName) {
        updateSubscription(newPlanName.trim());
    }
}

// Handle subscription update (upgrade/downgrade)
async function updateSubscription(newPlanName) {
    if (!newPlanName) {
        alert('Please enter a plan name.');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/update`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: currentUserId,
                newPlanName: newPlanName
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Update failed');
        }

        const data = await response.json();
        alert('Subscription updated successfully!');
        // Refresh dashboard
        loadUserDashboard();
    } catch (error) {
        console.error('Error updating subscription:', error);
        alert(error.message || 'Update failed. Please try again.');
    }
}

// Handle subscription cancellation
async function handleCancelSubscription() {
    if (!confirm('Are you sure you want to cancel your subscription?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/cancel`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: currentUserId
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Cancellation failed');
        }

        const data = await response.json();
        alert('Subscription cancelled successfully!');
        // Refresh dashboard to show cancelled status
        loadUserDashboard();
    } catch (error) {
        console.error('Error cancelling subscription:', error);
        alert(error.message || 'Cancellation failed. Please try again.');
    }
}

// Initialize the app when the DOM is loaded
document.addEventListener('DOMContentLoaded', init);