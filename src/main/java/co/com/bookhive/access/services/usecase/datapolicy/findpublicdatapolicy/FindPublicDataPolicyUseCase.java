package co.com.bookhive.access.services.usecase.datapolicy.findpublicdatapolicy;

import co.com.bookhive.access.domain.datapolicy.findpublicdatapolicy.PublicDataPolicyDomain;

public interface FindPublicDataPolicyUseCase {

	PublicDataPolicyDomain execute(String domain);

}
