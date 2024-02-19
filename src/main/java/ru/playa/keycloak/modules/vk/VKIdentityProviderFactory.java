package ru.playa.keycloak.modules.vk;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import ru.playa.keycloak.modules.AbstractVKOAuth2IdentityProvider;

import java.util.List;

/**
 * Фабрика OAuth-авторизации через <a href="https://vk.com">ВКонтакте</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class VKIdentityProviderFactory
        extends AbstractIdentityProviderFactory<VKIdentityProvider>
        implements SocialIdentityProviderFactory<VKIdentityProvider> {

    /**
     * Уникальный идентификатор провайдера.
     */
    public static final String PROVIDER_ID = "vk";

    @Override
    public String getName() {
        return "VK";
    }

    @Override
    public VKIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new VKIdentityProvider(session, new VKIdentityProviderConfig(model));
    }

    @Override
    public VKIdentityProviderConfig createConfig() {
        return new VKIdentityProviderConfig();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return AbstractVKOAuth2IdentityProvider.CONFIG_PROPERTIES;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}