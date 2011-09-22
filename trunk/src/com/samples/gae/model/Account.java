package com.samples.gae.model;

import com.zipeg.gae.*;

import javax.jdo.annotations.*;

import static com.zipeg.gae.obj.equal;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Account {

    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@PrimaryKey public String id;
    @Persistent public String nickname;
    @Persistent public String email;
    @Persistent public String federatedIdentity;
    @Persistent public String authDomain;

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Account) {
            Account a = (Account)o;
            return  equal(id, a.id) &&
                    equal(nickname, a.nickname) &&
                    equal(email, a.email) &&
                    equal(federatedIdentity, a.federatedIdentity) &&
                    equal(authDomain, a.authDomain);
        } else {
            return false;
        }
    }

}
