const api = '/api/subscriptions';
const subscriptionList = document.getElementById('subscription-list');
const totalMonthlyEl = document.getElementById('total-monthly');
const upcomingList = document.getElementById('upcoming-list');
const chartCategory = document.getElementById('chartCategory');
const chartStatus = document.getElementById('chartStatus');
const form = document.getElementById('subscription-form');
const formError = document.getElementById('form-error');
const formTitle = document.getElementById('form-title');
const cancelEdit = document.getElementById('cancel-edit');

const fields = ['name','category','cost','billingCycle','nextBillingDate','notes','status'];

async function loadAll() {
    try {
        const [subs, summary] = await Promise.all([fetch(api).then(r => r.json()), fetch(api + '/summary').then(r => r.json())]);
        renderSubscriptions(subs);
        renderSummary(summary);
    } catch (err) {
        formError.textContent = 'Unable to load data: ' + err.message;
    }
}

function renderSubscriptions(subs) {
    subscriptionList.innerHTML = '';
    if (!subs.length) {
        subscriptionList.innerHTML = '<p style="padding:.5rem; color:#556581;">No subscriptions added yet.</p>';
        return;
    }
    subs.forEach(sub => {
        const card = document.createElement('div');
        card.className = 'subscription-card';
        card.innerHTML = `
            <h4>${escapeHtml(sub.name)}</h4>
            <div class="meta">${escapeHtml(sub.category)} • ${escapeHtml(sub.status)}</div>
            <div class="meta">${sub.billingCycle} • $${Number(sub.cost).toFixed(2)}</div>
            <div class="meta">Next: ${sub.nextBillingDate}</div>
            <p>${escapeHtml(sub.notes || '')}</p>
            <div class="actions">
                <button onclick='editSubscription("${sub.id}")'>Edit</button>
                <button onclick='deleteSubscription("${sub.id}")' style='background:#ff5c7a'>Delete</button>
            </div>
        `;
        subscriptionList.appendChild(card);
    });
}

function escapeHtml(str) {
    return String(str || '').replace(/[&<>"] /g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',' ':' '})[c] || c);
}

async function renderSummary(summary) {
    totalMonthlyEl.textContent = '$' + Number(summary.totalMonthly || 0).toFixed(2);
    upcomingList.innerHTML = '';
    (summary.upcoming || []).forEach(item => {
        const li = document.createElement('li');
        li.textContent = `${item.name} - ${item.nextBillingDate} ($${Number(item.cost).toFixed(2)})`;
        upcomingList.appendChild(li);
    });

    const categories = summary.byCategory || {};
    const statuses = summary.byStatus || {};

    renderMiniChart(chartCategory, categories);
    renderMiniChart(chartStatus, statuses);
}

function renderMiniChart(canvas, items) {
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const keys = Object.keys(items);
    if (!keys.length) {
        ctx.fillStyle = '#8a97b0';
        ctx.font = '14px Inter, sans-serif';
        ctx.fillText('No data', 10, 20);
        return;
    }
    const max = Math.max(...keys.map(k => items[k]));
    const barWidth = (canvas.width - 20) / keys.length;
    keys.forEach((k, i) => {
        const x = 10 + i * barWidth;
        const y = canvas.height - 20;
        const height = max > 0 ? (items[k] / max) * (canvas.height - 40) : 0;
        ctx.fillStyle = '#5f7dff';
        ctx.fillRect(x, y - height, barWidth * .7, height);
        ctx.fillStyle = '#2a303d';
        ctx.font = '10px Inter, sans-serif';
        const text = `${k} (${items[k]})`;
        ctx.fillText(text, x, canvas.height - 2);
    });
}

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    formError.textContent = '';
    const payload = {};
    fields.forEach(f => payload[f] = document.getElementById(f).value);
    payload.cost = parseFloat(payload.cost);
    if (!payload.name || !payload.category || !payload.cost || !payload.nextBillingDate) {
        formError.textContent = 'Fill in required fields correctly.';
        return;
    }

    const subId = document.getElementById('sub-id').value;
    try {
        const opts = {
            method: subId ? 'PUT' : 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        };
        const endpoint = subId ? `${api}/${subId}` : api;
        const response = await fetch(endpoint, opts);
        if (!response.ok) {
            const errBody = await response.json().catch(() => ({}));
            const msg = errBody.message || JSON.stringify(errBody);
            throw new Error(msg || 'Failed to save.');
        }
        resetForm();
        loadAll();
    } catch (err) {
        formError.textContent = 'Failed to save: ' + err.message;
    }
});

window.editSubscription = async id => {
    try {
        const subs = await fetch(api).then(r => r.json());
        const sub = subs.find(s => s.id === id);
        if (!sub) throw new Error('Missing subscription');
        document.getElementById('sub-id').value = sub.id;
        document.getElementById('name').value = sub.name;
        document.getElementById('category').value = sub.category;
        document.getElementById('cost').value = sub.cost;
        document.getElementById('billingCycle').value = sub.billingCycle;
        document.getElementById('nextBillingDate').value = sub.nextBillingDate;
        document.getElementById('notes').value = sub.notes;
        document.getElementById('status').value = sub.status;
        formTitle.textContent = 'Edit Subscription';
    } catch (err) {
        formError.textContent = 'Unable to load for edit: ' + err.message;
    }
};

window.deleteSubscription = async id => {
    if (!confirm('Delete this subscription permanently?')) return;
    try {
        const r = await fetch(`${api}/${id}`, {method:'DELETE'});
        if (!r.ok) throw new Error('delete failed');
        loadAll();
    } catch (err) {
        formError.textContent = 'Delete failed: ' + err.message;
    }
};

cancelEdit.addEventListener('click', resetForm);

function resetForm() {
    form.reset();
    document.getElementById('sub-id').value = '';
    formError.textContent = '';
    formTitle.textContent = 'Add Subscription';
}

loadAll();
