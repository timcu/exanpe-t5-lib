//
// Copyright 2011 EXANPE <exanpe@gmail.com>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

/**
 * 
 */
package fr.exanpe.t5.lib.services;

import java.awt.Color;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.commons.services.Coercion;
import org.apache.tapestry5.commons.services.CoercionTuple;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.commons.util.StringToEnumCoercion;
import org.slf4j.Logger;

import fr.exanpe.t5.lib.annotation.Authorize;
import fr.exanpe.t5.lib.constants.AccordionEventTypeEnum;
import fr.exanpe.t5.lib.constants.DialogRenderModeEnum;
import fr.exanpe.t5.lib.constants.ExanpeSymbols;
import fr.exanpe.t5.lib.constants.GMapTypeEnum;
import fr.exanpe.t5.lib.constants.MenuEventTypeEnum;
import fr.exanpe.t5.lib.constants.PasswordStrengthCheckerTypeEnum;
import fr.exanpe.t5.lib.constants.SecurePasswordEventTypeEnum;
import fr.exanpe.t5.lib.constants.SliderOrientationTypeEnum;
import fr.exanpe.t5.lib.internal.authorize.AuthorizePageFilter;
import fr.exanpe.t5.lib.internal.authorize.AuthorizeWorker;
import fr.exanpe.t5.lib.internal.contextpagereset.ContextPageResetWorker;
import fr.exanpe.t5.lib.services.impl.AuthorizeBusinessServiceImpl;
import fr.exanpe.t5.lib.services.impl.LocaleSessionServiceImpl;

/**
 * The Tapestry Module for Exanpe Library.
 * 
 * @author lguerin
 */
public class ExanpeLibraryModule
{
    public static void contributeComponentClassResolver(Configuration<LibraryMapping> configuration, Logger log)
    {
        // Mapping for exanpe prexix
        configuration.add(new LibraryMapping("exanpe", "fr.exanpe.t5.lib"));
        log.info("Registering Exanpe library");
    }

    public static void contributeTypeCoercer(@SuppressWarnings("rawtypes")
    MappedConfiguration<CoercionTuple.Key, CoercionTuple> configuration)
    {        
        @SuppressWarnings("rawtypes")
        Class[] lstClass = new Class[] {
            SecurePasswordEventTypeEnum.class, 
            AccordionEventTypeEnum.class, 
            DialogRenderModeEnum.class, 
            SliderOrientationTypeEnum.class, 
            MenuEventTypeEnum.class, 
            PasswordStrengthCheckerTypeEnum.class, 
            GMapTypeEnum.class};
        
        for (@SuppressWarnings("rawtypes") Class cls : lstClass) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            CoercionTuple ct = CoercionTuple.create(String.class, cls, StringToEnumCoercion.create(cls));
            configuration.add(ct.getKey(), ct);
        }
        
        // ColorPicker
        Coercion<String, Color> coercionStringColor = new Coercion<String, Color>()
        {
            public Color coerce(String input)
            {
                if (StringUtils.isEmpty(input))
                    return null;
                return Color.decode("0x" + input);
            }
        };
        CoercionTuple<String, Color> ctStringColor = CoercionTuple.create(String.class, Color.class, coercionStringColor);
        configuration.add(ctStringColor.getKey(), ctStringColor);

        Coercion<Color, String> coercionColorString = new Coercion<Color, String>()
        {
            public String coerce(Color input)
            {
                if (input == null)
                    return null;

                String rgb = Integer.toHexString(input.getRGB());
                rgb = rgb.substring(2, rgb.length());
                return rgb;
            }
        };
        CoercionTuple<Color, String> ctColorString = CoercionTuple.create(Color.class, String.class, coercionColorString);
        configuration.add(ctColorString.getKey(), ctColorString);

    }

    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(ExanpeSymbols.ASSET_BASE, "classpath:/fr/exanpe/t5/lib/components");
        configuration.add(ExanpeSymbols.YUI2_BASE, "classpath:fr/exanpe/t5/lib/external/js/yui/2.9.0/");
        configuration.add(ExanpeSymbols.CONTEXT_PAGE_RESET_MARKER, "reset");
        configuration.add(ExanpeSymbols.GMAP_V3_API_KEY, "");
        configuration.add(ExanpeSymbols.GMAP_V3_BUSINESS_CLIENT_ID, "");
        configuration.add(ExanpeSymbols.GMAP_V3_VERSION, "3.6");
    }

    /**
     * Contribution for method {@link Authorize} annotation
     * 
     * @param configuration
     * @param locator
     * @param resolver
     */
    public static void contributeComponentClassTransformWorker(OrderedConfiguration<ComponentClassTransformWorker2> configuration, ObjectLocator locator,
            ComponentClassResolver resolver)
    {
        configuration.addInstance("AuthorizeWorker", AuthorizeWorker.class, "before:OnEvent");
        configuration.addInstance("ContextPageResetWorker", ContextPageResetWorker.class, "before:OnEvent");
    }

    /**
     * Contribution for page {@link Authorize} annotation
     * 
     * @param configuration
     */
    public void contributeComponentRequestHandler(OrderedConfiguration<ComponentRequestFilter> configuration)
    {
        configuration.addInstance("AuthorizePageFilter", AuthorizePageFilter.class);
    }

    public static void bind(ServiceBinder binder)
    {
        binder.bind(ExanpeComponentService.class, ExanpeComponentService.class);
        binder.bind(AuthorizeBusinessService.class, AuthorizeBusinessServiceImpl.class);
        binder.bind(LocaleSessionService.class, LocaleSessionServiceImpl.class);
    }
}
