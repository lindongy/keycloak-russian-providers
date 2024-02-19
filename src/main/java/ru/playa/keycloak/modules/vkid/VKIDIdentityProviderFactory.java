package ru.playa.keycloak.modules.vkid;

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
public class VKIDIdentityProviderFactory
        extends AbstractIdentityProviderFactory<VKIDIdentityProvider>
        implements SocialIdentityProviderFactory<VKIDIdentityProvider> {

    /**
     * Уникальный идентификатор провайдера.
     */
    public static final String PROVIDER_ID = "vkid";

    @Override
    public String getName() {
        return "VK ID";
    }

    @Override
    public VKIDIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new VKIDIdentityProvider(session, new VKIDIdentityProviderConfig(model));
    }

    @Override
    public VKIDIdentityProviderConfig createConfig() {
        return new VKIDIdentityProviderConfig();
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