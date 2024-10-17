package ru.playa.keycloak.modules.tinkoff;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;
import ru.playa.keycloak.modules.Utils;

/**
 * Настройки OAuth-авторизации через <a href="tinkoff.github.io">Tinkoff</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class TinkoffIdentityProviderConfig
        extends OAuth2IdentityProviderConfig {

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="tinkoff.github.io">Tinkoff</a>.
     *
     * @param model Модель настроек OAuth-авторизации.
     */
    public TinkoffIdentityProviderConfig(final IdentityProviderModel model) {
        super(model);
    }

    /**
     * Создает объект настроек OAuth-авторизации через
     * <a href="tinkoff.github.io">Tinkoff</a>.
     */
    public TinkoffIdentityProviderConfig() {
    }

    /**
     * Получения ИНН.
     *
     * @return ИНН.
     */
    public String getINN() {
        String inn = this.getConfig().get("inn");

        return Utils.nonNullOrEmpty(inn) ? inn : null;
    }

    /**
     * Получения КПП.
     *
     * @return КПП.
     */
    public String getKPP() {
        String kpp = this.getConfig().get("kpp");

        return Utils.nonNullOrEmpty(kpp) ? kpp : null;
    }
}
