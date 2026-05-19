import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { useState } from "react";

function AdminLayout({ children }) {
    const navigate = useNavigate();
    const location = useLocation();
    const { logout } = useAuth();
    const [showLogoutModal, setShowLogoutModal] = useState(false);

    const navItems = [
        { path: "/admin",        icon: "📊", label: "Dashboard" },
        { path: "/admin/items",  icon: "📋", label: "Item Management" },
        { path: "/admin/claims", icon: "🔖", label: "Claims" },
        { path: "/admin/users",  icon: "👥", label: "Users" },
    ];

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    return (
        <div className="admin-layout">
            {/* Sidebar */}
            <aside className="admin-sidebar">
                <div className="admin-sidebar-logo">FINDit</div>
                <nav className="admin-sidebar-nav">
                    {navItems.map(({ path, icon, label }) => {
                        const isActive = location.pathname === path;
                        return (
                            <button
                                key={path}
                                className={`admin-nav-item ${isActive ? "admin-nav-item-active" : ""}`}
                                onClick={() => navigate(path)}
                            >
                                <span className="admin-nav-icon">{icon}</span>
                                <span>{label}</span>
                            </button>
                        );
                    })}
                </nav>
                <button
                    className="admin-logout-btn"
                    onClick={() => setShowLogoutModal(true)}
                >
                    <span className="admin-nav-icon">🚪</span>
                    <span>Logout</span>
                </button>
            </aside>

            {/* Main content */}
            <main className="admin-main">
                {children}
            </main>

            {/* Logout Modal */}
            {showLogoutModal && (
                <div className="modal-overlay" onClick={() => setShowLogoutModal(false)}>
                    <div className="modal-card" onClick={e => e.stopPropagation()}>
                        <div className="modal-icon-wrap">
                            <span className="modal-icon">🚪</span>
                        </div>
                        <h3 className="modal-title">Log out of FINDit?</h3>
                        <p className="modal-body">
                            You will be signed out and redirected to the login page.
                        </p>
                        <div className="modal-actions">
                            <button className="btn-logout-confirm" onClick={handleLogout}>
                                Yes, Log Out
                            </button>
                            <button className="btn-modal-cancel" onClick={() => setShowLogoutModal(false)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default AdminLayout;
