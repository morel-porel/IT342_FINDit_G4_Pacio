import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import api from "../utils/api";

const CATEGORIES = [
    "Electronics",
    "Clothing",
    "Accessories",
    "Documents",
    "Keys",
    "Wallet / Bag",
    "Books",
    "Sports",
    "Other",
];

function ReportItemPage() {
    const navigate = useNavigate();
    const { register, handleSubmit, formState: { errors }, watch } = useForm({
        defaultValues: { type: "LOST" }
    });
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState(null);
    const selectedType = watch("type");

    const onSubmit = async (data) => {
        setSubmitting(true);
        setSubmitError(null);
        try {
            const payload = {
                type: data.type,
                name: data.name.trim(),
                category: data.category,
                description: data.description?.trim() || null,
                dateLostFound: data.dateLostFound,
                location: data.location.trim(),
                imageUrl: data.imageUrl?.trim() || null,
            };
            const response = await api.post("/items", payload);
            navigate(`/items/${response.data.id}`, { state: { justCreated: true } });
        } catch (err) {
            setSubmitError(
                err.response?.data || "Something went wrong. Please try again."
            );
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="dashboard-page">
            <nav className="topbar">
                <div className="topbar-logo">FINDit</div>
                <div className="topbar-actions">
                    <button className="btn-ghost" onClick={() => navigate("/dashboard")}>
                        ← Back
                    </button>
                </div>
            </nav>

            <div className="form-page-wrapper">
                <div className="form-card">
                    <h2 className="form-title">Report an Item</h2>
                    <p className="form-subtitle">
                        Help the community by reporting a lost or found item.
                    </p>

                    {submitError && (
                        <div className="alert alert-error">{submitError}</div>
                    )}

                    <form onSubmit={handleSubmit(onSubmit)}>
                        {/* Type toggle */}
                        <div className="field-group">
                            <label>Report Type</label>
                            <div className="type-toggle">
                                <label className={`type-option ${selectedType === "LOST" ? "type-option-active-lost" : ""}`}>
                                    <input
                                        type="radio"
                                        value="LOST"
                                        {...register("type", { required: true })}
                                    />
                                    🔍 I Lost Something
                                </label>
                                <label className={`type-option ${selectedType === "FOUND" ? "type-option-active-found" : ""}`}>
                                    <input
                                        type="radio"
                                        value="FOUND"
                                        {...register("type", { required: true })}
                                    />
                                    ✅ I Found Something
                                </label>
                            </div>
                        </div>

                        {/* Name */}
                        <div className="field-group">
                            <label>Item Name <span className="required">*</span></label>
                            <input
                                {...register("name", {
                                    required: "Item name is required",
                                    maxLength: { value: 100, message: "Max 100 characters" }
                                })}
                                placeholder="e.g. Black Samsung phone, Blue notebook"
                                type="text"
                            />
                            {errors.name && <span className="field-error">{errors.name.message}</span>}
                        </div>

                        {/* Category */}
                        <div className="field-group">
                            <label>Category <span className="required">*</span></label>
                            <select
                                {...register("category", { required: "Please select a category" })}
                            >
                                <option value="">— Select category —</option>
                                {CATEGORIES.map(c => (
                                    <option key={c} value={c}>{c}</option>
                                ))}
                            </select>
                            {errors.category && <span className="field-error">{errors.category.message}</span>}
                        </div>

                        {/* Date */}
                        <div className="field-group">
                            <label>Date {selectedType === "LOST" ? "Lost" : "Found"} <span className="required">*</span></label>
                            <input
                                {...register("dateLostFound", {
                                    required: "Date is required",
                                    validate: v => new Date(v) <= new Date() || "Date cannot be in the future"
                                })}
                                type="date"
                                max={new Date().toISOString().split("T")[0]}
                            />
                            {errors.dateLostFound && <span className="field-error">{errors.dateLostFound.message}</span>}
                        </div>

                        {/* Location */}
                        <div className="field-group">
                            <label>Location <span className="required">*</span></label>
                            <input
                                {...register("location", {
                                    required: "Location is required",
                                    maxLength: { value: 200, message: "Max 200 characters" }
                                })}
                                placeholder="e.g. Library 2nd Floor, Cafeteria, Room 301"
                                type="text"
                            />
                            {errors.location && <span className="field-error">{errors.location.message}</span>}
                        </div>

                        {/* Description */}
                        <div className="field-group">
                            <label>Description <span className="optional">(optional)</span></label>
                            <textarea
                                {...register("description", {
                                    maxLength: { value: 500, message: "Max 500 characters" }
                                })}
                                placeholder="Add any identifying details — color, brand, markings, contents..."
                                rows={3}
                            />
                            {errors.description && <span className="field-error">{errors.description.message}</span>}
                        </div>

                        {/* Image URL */}
                        <div className="field-group">
                            <label>Image URL <span className="optional">(optional)</span></label>
                            <input
                                {...register("imageUrl", {
                                    pattern: {
                                        value: /^https?:\/\/.+/,
                                        message: "Must be a valid URL starting with http:// or https://"
                                    }
                                })}
                                placeholder="https://..."
                                type="url"
                            />
                            {errors.imageUrl && <span className="field-error">{errors.imageUrl.message}</span>}
                        </div>

                        <button
                            type="submit"
                            className="btn-primary"
                            disabled={submitting}
                        >
                            {submitting ? "Submitting..." : "Submit Report"}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default ReportItemPage;