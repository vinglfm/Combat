public class LogicError extends Error {
	private static final String INTERNAL_ERROR = "There is an internal error in the logic, please contact developers team";

	public LogicError() {
		super(INTERNAL_ERROR);
	}
}
