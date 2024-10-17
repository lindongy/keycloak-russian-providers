package ru.playa.keycloak.modules.sber;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * Настройки OAuth-авторизации через <a href="https://developers.sber.ru/docs/ru/sberid/overview">SberID</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class SberIdentityProviderConfig
        extends OAuth2IdentityProviderConfig {

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://developers.sber.ru/docs/ru/sberid/overview">SberID</a>.
     *
     * @param model Модель настроек OAuth-авторизации.
     */
    public SberIdentityProviderConfig(final IdentityProviderModel model) {
        super(model);
    }

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="https://developers.sber.ru/docs/ru/sberid/overview">SberID</a>.
     */
    public SberIdentityProviderConfig() {
    }

    /**
     * Получения хоста.
     *
     * @return Хост.
     */
    public String getHost() {
        return getConfig().get("host");
    }

    /**
     * Получение URL запроса завершения авторизации.
     * @return URL запроса завершения авторизации.
     */
    public String getCompletedUrl() {
        return getConfig().get("completedUrl");
    }

    /**
     * Установка URL запроса завершения авторизации.
     *
     * @param completedUrl URL запроса завершения авторизации.
     */
    public void setCompletedUrl(final String completedUrl) {
        getConfig().put("completedUrl", completedUrl);
    }
}