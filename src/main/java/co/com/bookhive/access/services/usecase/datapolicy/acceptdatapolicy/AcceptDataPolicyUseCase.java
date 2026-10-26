package co.com.bookhive.access.services.usecase.datapolicy.acceptdatapolicy;

import co.com.bookhive.access.domain.datapolicy.acceptdatapolicy.AcceptDataPolicyDomain;
import co.com.bookhive.access.domain.datapolicy.finddatapolicy.DataPolicyDomain;

public interface AcceptDataPolicyUseCase {

	DataPolicyDomain execute(AcceptDataPolicyDomain acceptance);

}
