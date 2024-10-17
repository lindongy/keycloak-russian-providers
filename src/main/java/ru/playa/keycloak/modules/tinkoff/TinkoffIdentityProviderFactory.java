package ru.playa.keycloak.modules.tinkoff;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

/**
 * Фабрика OAuth-авторизации через <a href="tinkoff.github.io">Tinkoff</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class TinkoffIdentityProviderFactory
        extends AbstractIdentityProviderFactory<TinkoffIdentityProvider>
        implements SocialIdentityProviderFactory<TinkoffIdentityProvider> {

    /**
     * Уникальный идентификатор провайдера.
     */
    public static final String PROVIDER_ID = "tinkoff";

    @Override
    public String getName() {
        return "Tinkoff";
    }

    @Override
    public TinkoffIdentityProvider create(final KeycloakSession session, final IdentityProviderModel model) {
        return new TinkoffIdentityProvider(session, new TinkoffIdentityProviderConfig(model));
    }

    @Override
    public TinkoffIdentityProviderConfig createConfig() {
        return new TinkoffIdentityProviderConfig();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("inn")
                .label("INN")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property()
                .name("kpp")
                .label("KPP")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .build();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}