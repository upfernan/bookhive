package co.com.bookhive.catalog.services.output.ports;

import co.com.bookhive.catalog.domain.account.viewmyaccount.MyAccountDomain;

public interface UserAccountPort {

	MyAccountDomain findMine();

}
