package ru.playa.keycloak.modules.sber;

import ru.playa.keycloak.modules.AbstractRussianJsonUserAttributeMapper;

/**
 * Пользовательские аттрибуты необходимые для авторизации через
 * <a href="https://developers.sber.ru/docs/ru/sberid/overview">SberID</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class SberUserAttributeMapper
    extends AbstractRussianJsonUserAttributeMapper {

    /**
     * Список совместимых провайдеров.
     */
    private static final String[] COMPATIBLE_PROVIDERS = new String[]{SberIdentityProviderFactory.PROVIDER_ID};

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getId() {
        return "sber-user-attribute-mapper";
    }
}