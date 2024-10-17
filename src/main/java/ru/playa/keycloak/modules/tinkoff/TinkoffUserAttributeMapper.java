package ru.playa.keycloak.modules.tinkoff;

import ru.playa.keycloak.modules.AbstractRussianJsonUserAttributeMapper;

/**
 * Пользовательские аттрибуты необходимые для авторизации через
 * <a href="tinkoff.github.io">Tinkoff</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class TinkoffUserAttributeMapper
    extends AbstractRussianJsonUserAttributeMapper {

    /**
     * Список совместимых провайдеров.
     */
    private static final String[] COMPATIBLE_PROVIDERS = new String[]{TinkoffIdentityProviderFactory.PROVIDER_ID};

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getId() {
        return "tinkoff-user-attribute-mapper";
    }
}