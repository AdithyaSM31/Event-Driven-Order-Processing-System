import { useState, useEffect } from 'react'

function App() {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [username, setUsername] = useState('');
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  
  // Login State
  const [loginUser, setLoginUser] = useState('user123');

  // Create Order State
  const [productId, setProductId] = useState('PROD-001');
  const [productName, setProductName] = useState('Test Product');
  const [quantity, setQuantity] = useState(1);
  const [price, setPrice] = useState(100.0);

  const API_BASE = 'http://localhost:8080';

  const login = async () => {
    try {
      const res = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: loginUser, password: 'password' })
      });
      const data = await res.json();
      if (data.token) {
        setToken(data.token);
        setUsername(loginUser);
        localStorage.setItem('token', data.token);
        // fetchOrders(data.token);
      }
    } catch (e) {
      alert('Login Failed');
    }
  };

  const logout = () => {
    setToken(null);
    localStorage.removeItem('token');
    setOrders([]);
  };

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/orders/user`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setOrders(data);
      }
    } catch (e) {
      console.error(e);
    }
    setLoading(false);
  };

  const createOrder = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/orders`, {
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            items: [
                { productId, productName, quantity: parseInt(quantity), price: parseFloat(price) }
            ]
        })
      });
      if (res.ok) {
        alert('Order Placed!');
        fetchOrders(); // Refresh list
      } else {
        alert('Failed to place order');
      }
    } catch (e) {
      alert('Error placing order');
    }
  };

  useEffect(() => {
    if (token) {
        fetchOrders();
        const interval = setInterval(fetchOrders, 3000); // Auto-refresh for "event driven" feel
        return () => clearInterval(interval);
    }
  }, [token]);

  if (!token) {
    return (
      <div className="login-container">
        <h1>Welcome Back</h1>
        <p style={{ color: '#64748b' }}>Enter your credentials to access the system</p>
        <div className="login-form">
          <input 
            value={loginUser} 
            onChange={e => setLoginUser(e.target.value)} 
            placeholder="Username" 
          />
          <button onClick={login}>Sign In</button>
        </div>
      </div>
    );
  }

  return (
    <div>
      <header className="flex justify-between items-center" style={{ marginBottom: '40px' }}>
        <div className="flex items-center gap-4">
            <div style={{ width: 40, height: 40, borderRadius: 8, background: '#2563eb', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontSize: '1.5rem' }}>📦</div>
            <h1 style={{ margin: 0, fontSize: '1.5rem' }}>OrderSystem</h1>
        </div>
        <div className="flex items-center gap-4">
            <span style={{ fontSize: '0.9rem', color: '#64748b' }}>{username || loginUser}</span>
            <button onClick={logout} style={{ background: 'transparent', color: '#64748b', border: '1px solid #e5e7eb', padding: '8px 16px' }}>
                Logout
            </button>
        </div>
      </header>

      <section className="card">
        <h2>Create New Order</h2>
        <div className="grid grid-cols-4 gap-4" style={{ alignItems: 'end' }}>
            <div>
                <label>Product ID</label>
                <input value={productId} onChange={e => setProductId(e.target.value)} />
            </div>
            <div>
                <label>Product Name</label>
                <input value={productName} onChange={e => setProductName(e.target.value)} />
            </div>
            <div>
                <label>Quantity</label>
                <input type="number" value={quantity} onChange={e => setQuantity(e.target.value)} min="1" />
            </div>
            <div>
                <label>Price</label>
                <input type="number" value={price} onChange={e => setPrice(e.target.value)} />
            </div>
            <button onClick={createOrder} style={{ height: '42px' }}>
                Place Order
            </button>
        </div>
      </section>

      <section>
        <div className="flex justify-between items-center" style={{ marginBottom: '20px' }}>
            <h2>Recent Orders</h2>
            <button 
                onClick={fetchOrders} 
                disabled={loading}
                style={{ background: 'transparent', color: '#2563eb', padding: 0 }}
            >
                {loading ? 'Refreshing...' : 'Refresh list'}
            </button>
        </div>
        
        {orders.length === 0 ? (
            <div className="card" style={{ textAlign: 'center', color: '#64748b', padding: '40px' }}>
                No orders found. Start by placing one above!
            </div>
        ) : (
            <div className="grid gap-4">
                {orders.map(order => (
                    <div key={order.id} className="card" style={{ margin: 0, padding: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                            <div className="flex items-center gap-4" style={{ marginBottom: '8px' }}>
                                <span style={{ fontWeight: 600 }}>#{order.id}</span>
                                <span className="status-badge">
                                    <span className={`status status-${order.status}`}>{order.status}</span>
                                </span>
                            </div>
                            <div style={{ fontSize: '0.85rem', color: '#64748b' }}>
                                {new Date(order.createdAt).toLocaleString()} • ${order.totalAmount.toFixed(2)}
                            </div>
                        </div>
                        <div style={{ textAlign: 'right' }}>
                            {order.items.map((item, idx) => (
                                <span key={idx} className="order-item-list">
                                    {item.productName} <span style={{ opacity: 0.6 }}>x{item.quantity}</span>
                                </span>
                            ))}
                        </div>
                    </div>
                ))}
            </div>
        )}
      </section>
    </div>
  )

}

export default App
