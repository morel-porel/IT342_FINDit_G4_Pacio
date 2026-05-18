import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./features/auth/LoginPage";
import RegisterPage from "./features/auth/RegisterPage";
import Dashboard from "./features/items/Dashboard";
import ProfilePage from "./features/profile/ProfilePage";
import OAuth2Callback from "./features/auth/OAuth2CallbackPage";
import ReportItemPage from "./features/items/ReportItemPage";
import ItemDetailPage from "./features/items/ItemDetailPage";
import MyReportsPage from "./features/myReports/MyReportsPage";
import MyClaimsPage from "./features/myClaims/MyClaimsPage";
import AdminDashboardPage from "./features/admin/AdminDashboardPage";
import AdminItemsPage from "./features/admin/AdminItemsPage";
import AdminClaimsPage from "./features/admin/AdminClaimsPage";
import AdminUsersPage from "./features/admin/AdminUsersPage";
import AdminRoute from "./features/admin/AdminRoute";
import { useAuth } from "./features/auth/AuthContext";
import "./App.css";

function App() {
    const { isAuthenticated } = useAuth();
    return (
        <Router>
            <Routes>
                {/* Public routes */}
                <Route path="/login"    element={!isAuthenticated ? <LoginPage />    : <Navigate to="/dashboard" />} />
                <Route path="/register" element={!isAuthenticated ? <RegisterPage /> : <Navigate to="/dashboard" />} />
                <Route path="/oauth2/callback" element={<OAuth2Callback />} />

                {/* Authenticated user routes */}
                <Route path="/dashboard"  element={isAuthenticated ? <Dashboard />      : <Navigate to="/login" />} />
                <Route path="/profile"    element={isAuthenticated ? <ProfilePage />    : <Navigate to="/login" />} />
                <Route path="/report"     element={isAuthenticated ? <ReportItemPage /> : <Navigate to="/login" />} />
                <Route path="/items/:id"  element={<ItemDetailPage />} />
                <Route path="/my-reports" element={isAuthenticated ? <MyReportsPage />  : <Navigate to="/login" />} />
                <Route path="/my-claims"  element={isAuthenticated ? <MyClaimsPage />   : <Navigate to="/login" />} />

                {/* Admin-only routes (role guard inside AdminRoute) */}
                <Route path="/admin"        element={<AdminRoute><AdminDashboardPage /></AdminRoute>} />
                <Route path="/admin/items"  element={<AdminRoute><AdminItemsPage /></AdminRoute>} />
                <Route path="/admin/claims" element={<AdminRoute><AdminClaimsPage /></AdminRoute>} />
                <Route path="/admin/users"  element={<AdminRoute><AdminUsersPage /></AdminRoute>} />

                {/* Default */}
                <Route path="/" element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} />} />
            </Routes>
        </Router>
    );
}

export default App;
