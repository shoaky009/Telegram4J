package telegram4j.mtproto;

import telegram4j.tl.api.TlMethod;
import telegram4j.tl.mtproto.RpcError;

import java.util.function.Predicate;
import java.util.stream.IntStream;

/** Subtype of {@link MTProtoException} which receives on RPC response. */
public class RpcException extends MTProtoException {
    private static final long serialVersionUID = -4159674899075462143L;

    private final RpcError error;

    public RpcException(String message, RpcError error) {
        super(message);
        this.error = error;
    }

    /**
     * Create {@link Predicate} for throwable which matches on
     * {@code RpcException} with one of specified error codes.
     *
     * @param codes The array of rpc method errors codes
     * @return A {@link Predicate} for throwable which matches
     * on {@code RpcException} with one of specified error codes.
     */
    public static Predicate<Throwable> isErrorCode(int... codes) {
        return t -> {
            if (t instanceof RpcException) {
                RpcException t0 = (RpcException) t;

                return IntStream.of(codes).anyMatch(c -> t0.getError().errorCode() == c);
            }
            return false;
        };
    }

    static String prettyMethodName(TlMethod<?> method) {
        return method.getClass().getCanonicalName()
                .replace("telegram4j.tl.", "")
                .replace("request.", "")
                .replace("Immutable", "");
    }

    static RpcException create(RpcError error, DefaultMTProtoClient.PendingRequest request) {
        String orig = error.errorMessage();
        int argIdx = orig.indexOf("_X");
        String message = argIdx != -1 ? orig.substring(0, argIdx) : orig;
        String arg = argIdx != -1 ? orig.substring(argIdx) : null;

        String format = String.format("%s returned code: %d, message: %s%s",
                prettyMethodName(request.method), error.errorCode(),
                message, arg != null ? ", param: " + arg : "");

        return new RpcException(format, error);
    }

    /**
     * Gets original {@link RpcError} received as method response.
     *
     * @return The original {@link RpcError}.
     */
    public RpcError getError() {
        return error;
    }
}
