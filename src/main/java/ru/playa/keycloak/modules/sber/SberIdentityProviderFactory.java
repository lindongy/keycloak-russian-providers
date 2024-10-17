package ru.playa.keycloak.modules.sber;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

/**
 * Фабрика OAuth-авторизации через <a href="https://my.mail.ru">Мой Мир</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class SberIdentityProviderFactory
    extends AbstractIdentityProviderFactory<SberIdentityProvider>
    implements SocialIdentityProviderFactory<SberIdentityProvider> {

    /**
     * Уникальный идентификатор провайдера.
     */
    public static final String PROVIDER_ID = "sber";

    @Override
    public String getName() {
        return "Sber";
    }

    @Override
    public SberIdentityProvider create(final KeycloakSession session, final IdentityProviderModel model) {
        return new SberIdentityProvider(session, new SberIdentityProviderConfig(model));
    }

    @Override
    public SberIdentityProviderConfig createConfig() {
        return new SberIdentityProviderConfig();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder
            .create()
            .property()
            .name("host")
            .label("Host")
            .helpText("Host")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .build();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}