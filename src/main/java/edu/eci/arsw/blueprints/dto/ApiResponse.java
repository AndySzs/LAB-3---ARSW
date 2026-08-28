package edu.eci.arsw.blueprints.dto;

/**
 * Envoltura de respuesta uniforme para todos los endpoints de la API.
 *
 * @param code    código HTTP de la respuesta (200, 201, 202, 400, 404, ...)
 * @param message mensaje descriptivo del resultado de la operación
 * @param data    payload de la respuesta (puede ser null en errores)
 * @param <T>     tipo del payload
 */
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> of(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "execute ok", data);
    }
}