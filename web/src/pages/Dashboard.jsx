import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../utils/AuthContext";
import api from "../utils/api";

const CATEGORY_ICONS = {
    "Electronics": "💻",
    "Clothing": "👕",
    "Accessories": "🎒",
    "Documents": "📄",
    "Keys": "🔑",
    "Wallet / Bag": "👜",
    "Books": "📚",
    "Sports": "⚽",
    "Other": "📦",
};

function ItemCard({ item, onClick }) {
    const isLost = item.type === "LOST";
    const icon = CATEGORY_ICONS[item.category] || "📦";
    const date = new Date(item.dateLostFound).toLocaleDateString("en-US", {
        month: "short", day: "numeric", year: "numeric"
    });

    return (
        <div className="item-card" onClick={() => onClick(item.id)}>
            {item.imageUrl ? (
                <div className="item-card-img">
                    <img src={item.imageUrl} alt={item.name} />
                </div>
            ) : (
                <div className="item-card-img item-card-img-placeholder">
                    <span>{icon}</span>
                </div>
            )}
            <div className="item-card-body">
                <div className="item-card-header">
                    <span className={`item-badge ${isLost ? "badge-lost" : "badge-found"}`}>
                        {item.type}
                    </span>
                    <span className="item-card-category">{item.category}</span>
                </div>
                <div className="item-card-name">{item.name}</div>
                <div className="item-card-meta">
                    <span>📍 {item.location}</span>
                    <span>🗓 {date}</span>
                </div>
                <div className="item-card-reporter">
                    Reported by <strong>{item.reporter?.fullName || "Unknown"}</strong>
                </div>
            </div>
        </div>
    );
}

function Dashboard() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState("ALL"); // ALL, LOST, FOUND
    const [search, setSearch] = useState("");
    const [error, setError] = useState(null);

    const firstName = user?.fullName?.split(" ")[0] || "there";

    useEffect(() => {
        api.get("/items")
            .then(res => setItems(res.data))
            .catch(() => setError("Failed to load items. Please try again."))
            .finally(() => setLoading(false));
    }, []);

    const filtered = items.filter(item => {
        const matchesFilter = filter === "ALL" || item.type === filter;
        const matchesSearch =
            !search ||
            item.name.toLowerCase().includes(search.toLowerCase()) ||
            item.location.toLowerCase().includes(search.toLowerCase()) ||
            item.category.toLowerCase().includes(search.toLowerCase());
        return matchesFilter && matchesSearch;
    });

    const lostCount = items.filter(i => i.type === "LOST").length;
    const foundCount = items.filter(i => i.type === "FOUND").length;

    return (
        <div className="dashboard-page">
            <nav className="topbar">
                <div className="topbar-logo">FINDit</div>
                <div className="topbar-actions">
                    <button className="btn-primary btn-sm" onClick={() => navigate("/report")}>
                        + Report Item
                    </button>
                    <div className="avatar" onClick={() => navigate("/profile")}>
                        {user?.fullName?.split(" ").map(n => n[0]).join("").toUpperCase().slice(0, 2) || "?"}
                    </div>
                </div>
            </nav>

            <div className="dashboard-hero">
                <h2>Welcome back, {firstName}!</h2>
                <p>Browse lost and found items or report something you've found.</p>
                <div className="dashboard-stats">
                    <div className="stat-pill stat-lost">{lostCount} Lost</div>
                    <div className="stat-pill stat-found">{foundCount} Found</div>
                </div>
            </div>

            <div className="dashboard-controls">
                <input
                    className="search-input"
                    type="text"
                    placeholder="Search by name, location, or category..."
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
                <div className="filter-tabs">
                    {["ALL", "LOST", "FOUND"].map(f => (
                        <button
                            key={f}
                            className={`filter-tab ${filter === f ? "active" : ""}`}
                            onClick={() => setFilter(f)}
                        >
                            {f}
                        </button>
                    ))}
                </div>
            </div>

            <div className="dashboard-feed">
                {loading && <p className="loading-text">Loading items...</p>}
                {error && <p className="error-text">{error}</p>}
                {!loading && !error && filtered.length === 0 && (
                    <div className="empty-state">
                        <span>🔍</span>
                        <p>No items match your search.</p>
                        <button className="btn-primary btn-sm" onClick={() => navigate("/report")}>
                            Be the first to report
                        </button>
                    </div>
                )}
                <div className="item-grid">
                    {filtered.map(item => (
                        <ItemCard key={item.id} item={item} onClick={(id) => navigate(`/items/${id}`)} />
                    ))}
                </div>
            </div>
        </div>
    );
}

export default Dashboard;