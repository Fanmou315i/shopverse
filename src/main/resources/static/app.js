const api = async (path, options = {}) => {
  const response = await fetch(`/api${path}`, { headers: { 'Content-Type': 'application/json' }, ...options });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || '操作暂时未完成，请稍后重试');
  }
  return response.status === 204 ? null : response.json();
};

const state = { home: null, cart: null, activeCategory: '' };
const money = value => `¥${Number(value).toFixed(2)}`;
const escapeHtml = value => String(value).replace(/[&<>'"]/g, char => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', "'":'&#39;', '"':'&quot;' })[char]);
const showToast = text => { const toast = document.querySelector('#toast'); toast.textContent = text; toast.classList.add('visible'); window.clearTimeout(showToast.timer); showToast.timer = window.setTimeout(() => toast.classList.remove('visible'), 2200); };

function productCard(product) {
  return `<article class="product-card"><div class="product-art art-${product.category}"><span>${product.emoji}</span><em>${product.category}</em></div><div class="product-info"><p class="product-meta">${'★'.repeat(product.rating)} <span>${product.soldCount}+ 已购</span></p><h3>${escapeHtml(product.name)}</h3><p class="product-desc">${escapeHtml(product.description)}</p><div class="product-bottom"><strong>${money(product.price)}</strong><button class="round-add" onclick="addToCart(${product.id})" aria-label="加入购物车">+</button></div></div></article>`;
}

async function renderHome() {
  state.home = await api('/home');
  document.querySelector('#campaign').innerHTML = `<div><p>${state.home.campaign.subtitle}</p><h2>${state.home.campaign.title}</h2><span>优惠码：${state.home.campaign.code}</span></div><div class="campaign-orb">40<span>OFF</span></div>`;
  document.querySelector('#categories').innerHTML = state.home.categories.map(category => `<button class="category-chip" onclick="filterCategory('${category.name}')"><span>${category.icon}</span>${category.name}</button>`).join('');
  renderProducts(state.home.recommendations);
}

function renderProducts(products, title = '为你推荐') {
  document.querySelector('#products-title').textContent = title;
  document.querySelector('#products').innerHTML = products.length ? products.map(productCard).join('') : `<div class="empty-state"><span>⌕</span><h3>没有找到匹配商品</h3><p>换个关键词或看看其他分区吧。</p></div>`;
  document.querySelector('#reset-filter').hidden = title === '为你推荐';
}

async function filterCategory(category) {
  state.activeCategory = category;
  const products = await api(`/products?category=${encodeURIComponent(category)}`);
  renderProducts(products, `${category}好物`);
}

async function searchProducts(query) {
  const products = await api(`/products?query=${encodeURIComponent(query)}`);
  renderProducts(products, query ? `“${escapeHtml(query)}” 的结果` : '为你推荐');
}

async function addToCart(productId) {
  await api('/cart/items', { method: 'POST', body: JSON.stringify({ productId }) });
  showToast('已加入购物车');
  await loadCart();
}

async function loadMessages() {
  const messages = await api('/messages');
  const list = document.querySelector('#message-list');
  list.innerHTML = messages.map(message => `<div class="bubble-row ${message.sender === '我' ? 'mine' : ''}"><div class="bubble"><small>${escapeHtml(message.sender)}</small><p>${escapeHtml(message.content)}</p></div></div>`).join('');
  list.scrollTop = list.scrollHeight;
}

async function sendMessage(event) {
  event.preventDefault();
  const input = document.querySelector('#message-input');
  const content = input.value.trim();
  if (!content) return;
  await api('/messages', { method: 'POST', body: JSON.stringify({ content }) });
  input.value = '';
  await loadMessages();
}

function cartLine(item) {
  return `<article class="cart-line"><label class="check"><input type="checkbox" ${item.selected ? 'checked' : ''} onchange="setCartItem(${item.id}, ${item.quantity}, this.checked)" /><span></span></label><div class="cart-art">${item.emoji}</div><div class="cart-main"><h3>${escapeHtml(item.productName)}</h3><p>七天无理由 · 极速发货</p><strong>${money(item.unitPrice)}</strong></div><div class="quantity"><button onclick="setCartItem(${item.id}, ${Math.max(1, item.quantity - 1)}, ${item.selected})">−</button><span>${item.quantity}</span><button onclick="setCartItem(${item.id}, ${item.quantity + 1}, ${item.selected})">+</button></div><button class="delete" onclick="removeCartItem(${item.id})" aria-label="移除商品">×</button></article>`;
}

async function loadCart() {
  state.cart = await api('/cart');
  const list = document.querySelector('#cart-list');
  list.innerHTML = state.cart.items.length ? state.cart.items.map(cartLine).join('') : `<div class="empty-state cart-empty"><span>▱</span><h3>购物车还是空的</h3><p>去首页看看今天的精选好物。</p><button onclick="switchPage('home')">去逛逛</button></div>`;
  document.querySelector('#cart-select-all').textContent = state.cart.allSelected ? '取消全选' : '全选';
  document.querySelector('#cart-summary').innerHTML = state.cart.items.length ? `<div><small>已选 ${state.cart.selectedCount} 件</small><strong>${money(state.cart.total)}</strong></div><button onclick="checkout()">结算</button>` : '';
  const badge = document.querySelector('#cart-badge');
  badge.textContent = state.cart.items.reduce((sum, item) => sum + item.quantity, 0); badge.hidden = !state.cart.items.length;
}

async function setCartItem(id, quantity, selected) { await api(`/cart/items/${id}`, { method: 'PUT', body: JSON.stringify({ quantity, selected }) }); await loadCart(); }
async function removeCartItem(id) { await api(`/cart/items/${id}`, { method: 'DELETE' }); showToast('商品已移除'); await loadCart(); }
async function selectAll() { await api('/cart/selection', { method: 'PUT', body: JSON.stringify({ selected: !state.cart.allSelected }) }); await loadCart(); }
async function checkout() { const cart = await api('/cart'); await startCheckout('cart', cart.items.filter(item => item.selected)); }

async function loadProfile() {
  const profile = await api('/profile');
  const user = profile.user;
  document.querySelector('#profile').innerHTML = `<div class="profile-hero"><div class="avatar avatar-user">${user.avatar}</div><div><p class="eyebrow">WELCOME BACK</p><h1>${escapeHtml(user.name)}</h1><span>${user.level} · ${user.points} 积分</span></div><button class="settings" aria-label="设置">⚙</button></div><section class="stats"><div><strong>${profile.wallet.coupons}</strong><span>优惠券</span></div><div><strong>${profile.wallet.favorites}</strong><span>收藏</span></div><div><strong>${profile.wallet.footprints}</strong><span>足迹</span></div></section><section class="profile-section"><div class="section-heading"><h2>我的订单</h2><span>全部订单 ›</span></div>${profile.orders.map(order => `<div class="order-line"><div><strong>${order.status}</strong><p>${order.orderNo} · ${order.itemCount} 件商品</p></div><b>${money(order.total)}</b></div>`).join('')}</section><section class="profile-section"><div class="section-heading"><h2>常用服务</h2></div><div class="service-grid"><button>⌂<span>收货地址</span></button><button>♡<span>商品收藏</span></button><button>◌<span>客服帮助</span></button><button>⚙<span>账户设置</span></button></div></section><section class="address"><span>⌖</span><div><small>默认收货地址</small><p>${escapeHtml(profile.addresses[0].name)} ${profile.addresses[0].phone}<br/>${escapeHtml(profile.addresses[0].address)}</p></div><b>›</b></section>`;
}

function switchPage(page) {
  document.querySelectorAll('.page').forEach(element => element.classList.toggle('active', element.id === `page-${page}`));
  document.querySelectorAll('.nav-item').forEach(element => element.classList.toggle('active', element.dataset.page === page));
  if (page === 'messages') loadMessages();
  if (page === 'cart') loadCart();
  if (page === 'profile') loadProfile();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

document.querySelectorAll('.nav-item').forEach(button => button.addEventListener('click', () => switchPage(button.dataset.page)));
document.querySelector('#search-form').addEventListener('submit', event => { event.preventDefault(); searchProducts(document.querySelector('#search-input').value.trim()); });
document.querySelector('#message-form').addEventListener('submit', sendMessage);
document.querySelector('#cart-select-all').addEventListener('click', selectAll);
document.querySelector('#reset-filter').addEventListener('click', () => { document.querySelector('#search-input').value = ''; renderProducts(state.home.recommendations); });
window.addToCart = addToCart; window.filterCategory = filterCategory; window.setCartItem = setCartItem; window.removeCartItem = removeCartItem; window.checkout = checkout; window.switchPage = switchPage;
renderHome().catch(error => showToast(error.message));
loadCart().catch(() => {});
