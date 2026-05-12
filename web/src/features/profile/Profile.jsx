import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import api from "../../shared/api/api";

function Profile() {
    const { user, setUser, logout } = useAuth();
    const navigate = useNavigate();
    const [showLogoutModal, setShowLogoutModal] = useState(false);

    useEffect(() => {
        api.get("/auth/me")
            .then(res => setUser(res.data))
            .catch(() => {
                logout();
                navigate("/login");
            });
    }, []);

    const handleLogoutConfirm = () => {
        logout();
        navigate("/login");
    };

    if (!user) return <p className="loading-text">Loading profile...</p>;

    const initials = user.fullName
        ? user.fullName.split(" ").map(n => n[0]).join("").toUpperCase().slice(0, 2)
        : "??";

    return (
        <div className="profile-page">
            <div className="profile-header">
                <div className="profile-avatar">{initials}</div>
                <div className="profile-header-info">
                    <div className="profile-name">{user.fullName}</div>
                    <div className="profile-email">{user.email}</div>
                    <span className="profile-role-badge">{user.role}</span>
                </div>
                <button className="btn-outline-light">Edit Profile</button>
            </div>

            <div className="profile-section">
                <div className="profile-section-title">Account Information</div>
                <div className="profile-field-row">
                    <span className="profile-field-label">Full Name</span>
                    <span className="profile-field-value">{user.fullName}</span>
                </div>
                <div className="profile-field-row">
                    <span className="profile-field-label">Email</span>
                    <span className="profile-field-value">{user.email}</span>
                </div>
                <div className="profile-field-row">
                    <span className="profile-field-label">Member Since</span>
                    <span className="profile-field-value">
                        {user.createdAt
                            ? new Date(user.createdAt).toLocaleDateString("en-US", {
                                year: "numeric", month: "long", day: "numeric"
                              })
                            : "—"}
                    </span>
                </div>
            </div>

            <div className="profile-menu">
                <div className="profile-menu-item" onClick={() => navigate("/my-reports")}>
                    <span>📋 My Reports</span><span className="chevron">›</span>
                </div>
                <div className="profile-menu-item" onClick={() => navigate("/my-claims")}>
                    <span>🔖 My Claims</span><span className="chevron">›</span>
                </div>
                <div className="profile-menu-item">
                    <span>🔒 Change Password</span><span className="chevron">›</span>
                </div>
                <div className="profile-menu-item danger" onClick={() => setShowLogoutModal(true)}>
                    <span>🚪 Log Out</span><span className="chevron">›</span>
                </div>
            </div>

            {/* ── Logout confirmation modal ── */}
            {showLogoutModal && (
                <div className="modal-overlay" onClick={() => setShowLogoutModal(false)}>
                    <div className="modal-card" onClick={e => e.stopPropagation()}>
                        <div className="modal-icon-wrap">
                            <span className="modal-icon">🚪</span>
                        </div>
                        <h3 className="modal-title">Log out of FINDit?</h3>
                        <p className="modal-body">
                            You will be signed out of your account and redirected to the login page.
                        </p>
                        <div className="modal-actions">
                            <button className="btn-logout-confirm" onClick={handleLogoutConfirm}>
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

export default Profile;