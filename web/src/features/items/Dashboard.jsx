import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import api from "../../shared/api/api";

const CATEGORY_ICONS = {
    "Electronics": "💻", "Clothing": "👕", "Accessories": "🎒",
    "Documents": "📄", "Keys": "🔑", "Wallet / Bag": "👜",
    "Books": "📚", "Sports": "⚽", "Other": "📦",
};

const STATUS_META = {
    "OPEN":     { label: "Open",     cls: "status-open" },
    "PENDING":  { label: "Pending",  cls: "status-pending" },
    "APPROVED": { label: "Approved", cls: "status-approved" },
    "REJECTED": { label: "Rejected", cls: "status-rejected" },
    "RESOLVED": { label: "Resolved", cls: "status-resolved" },
};

function ItemCard({ item, onClick }) {
    const isLost = item.type === "LOST";
    const icon = CATEGORY_ICONS[item.category] || "📦";
    const date = new Date(item.dateLostFound).toLocaleDateString("en-US", {
        month: "short", day: "numeric",
    });
    const sm = STATUS_META[item.status] || { label: item.status, cls: "status-open" };

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
                <div className="item-card-name">{item.name}</div>
                <div className="item-card-meta">
                    <span>📍 {item.location}</span>
                    <span>· {date}</span>
                </div>
                <div className="item-card-footer">
                    <span className={`item-badge ${isLost ? "badge-lost" : "badge-found"}`}>
                        {isLost ? "Lost" : "Found"}
                    </span>
                    <span className={`status-badge ${sm.cls}`}>{sm.label}</span>
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
    const [typeFilter, setTypeFilter] = useState("ALL");
    const [categoryFilter, setCategoryFilter] = useState("ALL");
    const [statusFilter, setStatusFilter] = useState("ALL");
    const [search, setSearch] = useState("");
    const [error, setError] = useState(null);

    const initials = user?.fullName?.split(" ").map(n => n[0]).join("").toUpperCase().slice(0, 2) || "?";

    useEffect(() => {
        api.get("/items")
            .then(res => setItems(res.data))
            .catch(() => setError("Failed to load items. Please try again."))
            .finally(() => setLoading(false));
    }, []);

    const categories = ["ALL", ...Array.from(new Set(items.map(i => i.category))).sort()];

    const filtered = items.filter(item => {
        const matchesType = typeFilter === "ALL" || item.type === typeFilter;
        const matchesCat  = categoryFilter === "ALL" || item.category === categoryFilter;
        const matchesSt   = statusFilter === "ALL" || item.status === statusFilter;
        const matchesSrc  = !search ||
            item.name.toLowerCase().includes(search.toLowerCase()) ||
            item.location.toLowerCase().includes(search.toLowerCase()) ||
            item.category.toLowerCase().includes(search.toLowerCase());
        return matchesType && matchesCat && matchesSt && matchesSrc;
    });

    return (
        <div className="dashboard-page">
            {/* ── Topbar with inline search ── */}
            <nav className="topbar">
                <div className="topbar-logo">FINDit</div>
                <div className="topbar-search">
                    <span className="topbar-search-icon">🔍</span>
                    <input
                        type="text"
                        className="topbar-search-input"
                        placeholder="Search lost or found items..."
                        value={search}
                        onChange={e => setSearch(e.target.value)}
                    />
                </div>
                <div className="topbar-actions">
                    <button className="btn-primary btn-sm" onClick={() => navigate("/report")}>
                        + Report Item
                    </button>
                    <div className="avatar" onClick={() => navigate("/profile")}>
                        {initials}
                    </div>
                </div>
            </nav>

            {/* ── Filter bar ── */}
            <div className="filter-bar">
                <div className="filter-tabs">
                    {[
                        { val: "ALL",   label: "All Items" },
                        { val: "LOST",  label: "Lost" },
                        { val: "FOUND", label: "Found" },
                    ].map(({ val, label }) => (
                        <button
                            key={val}
                            className={`filter-tab ${typeFilter === val ? "active" : ""}`}
                            onClick={() => setTypeFilter(val)}
                        >
                            {label}
                        </button>
                    ))}
                </div>
                <div className="filter-right">
                    <select className="filter-select" value={categoryFilter}
                        onChange={e => setCategoryFilter(e.target.value)}>
                        {categories.map(c => (
                            <option key={c} value={c}>{c === "ALL" ? "All Categories" : c}</option>
                        ))}
                    </select>
                    <select className="filter-select" value={statusFilter}
                        onChange={e => setStatusFilter(e.target.value)}>
                        {["ALL", "OPEN", "PENDING", "APPROVED", "REJECTED", "RESOLVED"].map(s => (
                            <option key={s} value={s}>
                                {s === "ALL" ? "All Status" : s.charAt(0) + s.slice(1).toLowerCase()}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {/* ── Item grid ── */}
            <div className="dashboard-feed">
                {loading && <p className="loading-text">Loading items...</p>}
                {error   && <p className="error-text">{error}</p>}
                {!loading && !error && filtered.length === 0 && (
                    <div className="empty-state">
                        <span>🔍</span>
                        <p>No items found.</p>
                        <button className="btn-primary btn-sm" onClick={() => navigate("/report")}>
                            Be the first to report
                        </button>
                    </div>
                )}
                <div className="item-grid">
                    {filtered.map(item => (
                        <ItemCard key={item.id} item={item}
                            onClick={id => navigate(`/items/${id}`)} />
                    ))}
                </div>
            </div>
        </div>
    );
}

export default Dashboard;