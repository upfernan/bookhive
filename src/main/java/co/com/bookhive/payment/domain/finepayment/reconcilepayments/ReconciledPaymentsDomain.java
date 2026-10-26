package co.com.bookhive.payment.domain.finepayment.reconcilepayments;

public record ReconciledPaymentsDomain(int institutions, int reviewed, int finesPaid) {

	public ReconciledPaymentsDomain {
		institutions = Math.max(institutions, 0);
		reviewed = Math.max(reviewed, 0);
		finesPaid = Math.max(finesPaid, 0);
	}

}
