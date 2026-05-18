import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

function AdminRoute({ children }) {
    const { isAuthenticated, user } = useAuth();
    if (!isAuthenticated) return <Navigate to="/login" />;
    if (user?.role !== "ADMIN") return <Navigate to="/dashboard" />;
    return children;
}

export default AdminRoute;
