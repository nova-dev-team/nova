
/**
 * This is a generated class and is not intended for modification.  
 */
package valueObjects
{
import com.adobe.fiber.styles.IStyle;
import com.adobe.fiber.styles.Style;
import com.adobe.fiber.styles.StyleValidator;
import com.adobe.fiber.valueobjects.AbstractEntityMetadata;
import com.adobe.fiber.valueobjects.AvailablePropertyIterator;
import com.adobe.fiber.valueobjects.IPropertyIterator;
import mx.events.ValidationResultEvent;
import valueObjects.Users_type;
import com.adobe.fiber.core.model_internal;
import com.adobe.fiber.valueobjects.IModelType;
import mx.events.PropertyChangeEvent;

use namespace model_internal;

[ExcludeClass]
internal class _UserListRepEntityMetadata extends com.adobe.fiber.valueobjects.AbstractEntityMetadata
{
    private static var emptyArray:Array = new Array();

    model_internal static var allProperties:Array = new Array("message", "users", "pages_total", "success");
    model_internal static var allAssociationProperties:Array = new Array("users");
    model_internal static var allRequiredProperties:Array = new Array("message", "users", "pages_total", "success");
    model_internal static var allAlwaysAvailableProperties:Array = new Array("message", "users", "pages_total", "success");
    model_internal static var guardedProperties:Array = new Array();
    model_internal static var dataProperties:Array = new Array("message", "users", "pages_total", "success");
    model_internal static var derivedProperties:Array = new Array();
    model_internal static var collectionProperties:Array = new Array("users");
    model_internal static var collectionBaseMap:Object;
    model_internal static var entityName:String = "UserListRep";
    model_internal static var dependentsOnMap:Object;
    model_internal static var dependedOnServices:Array = new Array();

    
    model_internal var _messageIsValid:Boolean;
    model_internal var _messageValidator:com.adobe.fiber.styles.StyleValidator;
    model_internal var _messageIsValidCacheInitialized:Boolean = false;
    model_internal var _messageValidationFailureMessages:Array;
    
    model_internal var _usersIsValid:Boolean;
    model_internal var _usersValidator:com.adobe.fiber.styles.StyleValidator;
    model_internal var _usersIsValidCacheInitialized:Boolean = false;
    model_internal var _usersValidationFailureMessages:Array;
    
    model_internal var _pages_totalIsValid:Boolean;
    model_internal var _pages_totalValidator:com.adobe.fiber.styles.StyleValidator;
    model_internal var _pages_totalIsValidCacheInitialized:Boolean = false;
    model_internal var _pages_totalValidationFailureMessages:Array;

    model_internal var _instance:_Super_UserListRep;
    model_internal static var _nullStyle:com.adobe.fiber.styles.Style = new com.adobe.fiber.styles.Style();

    public function _UserListRepEntityMetadata(value : _Super_UserListRep)
    {
        // initialize property maps
        if (model_internal::dependentsOnMap == null)
        {
            // depenents map
            model_internal::dependentsOnMap = new Object();
            model_internal::dependentsOnMap["message"] = new Array();
            model_internal::dependentsOnMap["users"] = new Array();
            model_internal::dependentsOnMap["pages_total"] = new Array();
            model_internal::dependentsOnMap["success"] = new Array();

            // collection base map
            model_internal::collectionBaseMap = new Object()
            model_internal::collectionBaseMap["users"] = "valueObjects.Users_type";
        }

        model_internal::_instance = value;
        model_internal::_messageValidator = new StyleValidator(model_internal::_instance.model_internal::_doValidationForMessage);
        model_internal::_messageValidator.required = true;
        model_internal::_messageValidator.requiredFieldError = "message is required";
        //model_internal::_messageValidator.source = model_internal::_instance;
        //model_internal::_messageValidator.property = "message";
        model_internal::_usersValidator = new StyleValidator(model_internal::_instance.model_internal::_doValidationForUsers);
        model_internal::_usersValidator.required = true;
        model_internal::_usersValidator.requiredFieldError = "users is required";
        //model_internal::_usersValidator.source = model_internal::_instance;
        //model_internal::_usersValidator.property = "users";
        model_internal::_pages_totalValidator = new StyleValidator(model_internal::_instance.model_internal::_doValidationForPages_total);
        model_internal::_pages_totalValidator.required = true;
        model_internal::_pages_totalValidator.requiredFieldError = "pages_total is required";
        //model_internal::_pages_totalValidator.source = model_internal::_instance;
        //model_internal::_pages_totalValidator.property = "pages_total";
    }

    override public function getEntityName():String
    {
        return model_internal::entityName;
    }

    override public function getProperties():Array
    {
        return model_internal::allProperties;
    }

    override public function getAssociationProperties():Array
    {
        return model_internal::allAssociationProperties;
    }

    override public function getRequiredProperties():Array
    {
         return model_internal::allRequiredProperties;   
    }

    override public function getDataProperties():Array
    {
        return model_internal::dataProperties;
    }

    override public function getGuardedProperties():Array
    {
        return model_internal::guardedProperties;
    }

    override public function getUnguardedProperties():Array
    {
        return model_internal::allAlwaysAvailableProperties;
    }

    override public function getDependants(propertyName:String):Array
    {
       if (model_internal::dataProperties.indexOf(propertyName) == -1)
            throw new Error(propertyName + " is not a data property of entity UserListRep");  
            
       return model_internal::dependentsOnMap[propertyName] as Array;  
    }

    override public function getDependedOnServices():Array
    {
        return model_internal::dependedOnServices;
    }

    override public function getCollectionProperties():Array
    {
        return model_internal::collectionProperties;
    }

    override public function getCollectionBase(propertyName:String):String
    {
        if (model_internal::collectionProperties.indexOf(propertyName) == -1)
            throw new Error(propertyName + " is not a collection property of entity UserListRep");  

        return model_internal::collectionBaseMap[propertyName];
    }

    override public function getAvailableProperties():com.adobe.fiber.valueobjects.IPropertyIterator
    {
        return new com.adobe.fiber.valueobjects.AvailablePropertyIterator(this);
    }

    override public function getValue(propertyName:String):*
    {
        if (model_internal::allProperties.indexOf(propertyName) == -1)
        {
            throw new Error(propertyName + " does not exist for entity UserListRep");
        }

        return model_internal::_instance[propertyName];
    }

    override public function setValue(propertyName:String, value:*):void
    {
        if (model_internal::dataProperties.indexOf(propertyName) == -1)
        {
            throw new Error(propertyName + " is not a data property of entity UserListRep");
        }

        model_internal::_instance[propertyName] = value;
    }

    override public function getMappedByProperty(associationProperty:String):String
    {
        switch(associationProperty)
        {
            default:
            {
                return null;
            }
        }
    }

    override public function getPropertyLength(propertyName:String):int
    {
        switch(propertyName)
        {
            default:
            {
                return 0;
            }
        }
    }

    override public function isAvailable(propertyName:String):Boolean
    {
        if (model_internal::allProperties.indexOf(propertyName) == -1)
        {
            throw new Error(propertyName + " does not exist for entity UserListRep");
        }

        if (model_internal::allAlwaysAvailableProperties.indexOf(propertyName) != -1)
        {
            return true;
        }

        switch(propertyName)
        {
            default:
            {
                return true;
            }
        }
    }

    override public function getIdentityMap():Object
    {
        var returnMap:Object = new Object();

        return returnMap;
    }

    [Bindable(event="propertyChange")]
    override public function get invalidConstraints():Array
    {
        if (model_internal::_instance.model_internal::_cacheInitialized_isValid)
        {
            return model_internal::_instance.model_internal::_invalidConstraints;
        }
        else
        {
            // recalculate isValid
            model_internal::_instance.model_internal::_isValid = model_internal::_instance.model_internal::calculateIsValid();
            return model_internal::_instance.model_internal::_invalidConstraints;        
        }
    }

    [Bindable(event="propertyChange")]
    override public function get validationFailureMessages():Array
    {
        if (model_internal::_instance.model_internal::_cacheInitialized_isValid)
        {
            return model_internal::_instance.model_internal::_validationFailureMessages;
        }
        else
        {
            // recalculate isValid
            model_internal::_instance.model_internal::_isValid = model_internal::_instance.model_internal::calculateIsValid();
            return model_internal::_instance.model_internal::_validationFailureMessages;
        }
    }

    override public function getDependantInvalidConstraints(propertyName:String):Array
    {
        var dependants:Array = getDependants(propertyName);
        if (dependants.length == 0)
        {
            return emptyArray;
        }

        var currentlyInvalid:Array = invalidConstraints;
        if (currentlyInvalid.length == 0)
        {
            return emptyArray;
        }

        var filterFunc:Function = function(element:*, index:int, arr:Array):Boolean
        {
            return dependants.indexOf(element) > -1;
        }

        return currentlyInvalid.filter(filterFunc);
    }

    /**
     * isValid
     */
    [Bindable(event="propertyChange")] 
    public function get isValid() : Boolean
    {
        if (model_internal::_instance.model_internal::_cacheInitialized_isValid)
        {
            return model_internal::_instance.model_internal::_isValid;
        }
        else
        {
            // recalculate isValid
            model_internal::_instance.model_internal::_isValid = model_internal::_instance.model_internal::calculateIsValid();
            return model_internal::_instance.model_internal::_isValid;
        }
    }

    [Bindable(event="propertyChange")]
    public function get isMessageAvailable():Boolean
    {
        return true;
    }

    [Bindable(event="propertyChange")]
    public function get isUsersAvailable():Boolean
    {
        return true;
    }

    [Bindable(event="propertyChange")]
    public function get isPages_totalAvailable():Boolean
    {
        return true;
    }

    [Bindable(event="propertyChange")]
    public function get isSuccessAvailable():Boolean
    {
        return true;
    }


    /**
     * derived property recalculation
     */
    public function invalidateDependentOnMessage():void
    {
        if (model_internal::_messageIsValidCacheInitialized )
        {
            model_internal::_instance.model_internal::_doValidationCacheOfMessage = null;
            model_internal::calculateMessageIsValid();
        }
    }
    public function invalidateDependentOnUsers():void
    {
        if (model_internal::_usersIsValidCacheInitialized )
        {
            model_internal::_instance.model_internal::_doValidationCacheOfUsers = null;
            model_internal::calculateUsersIsValid();
        }
    }
    public function invalidateDependentOnPages_total():void
    {
        if (model_internal::_pages_totalIsValidCacheInitialized )
        {
            model_internal::_instance.model_internal::_doValidationCacheOfPages_total = null;
            model_internal::calculatePages_totalIsValid();
        }
    }

    model_internal function fireChangeEvent(propertyName:String, oldValue:Object, newValue:Object):void
    {
        this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, propertyName, oldValue, newValue));
    }

    [Bindable(event="propertyChange")]   
    public function get messageStyle():com.adobe.fiber.styles.Style
    {
        return model_internal::_nullStyle;
    }

    public function get messageValidator() : StyleValidator
    {
        return model_internal::_messageValidator;
    }

    model_internal function set _messageIsValid_der(value:Boolean):void 
    {
        var oldValue:Boolean = model_internal::_messageIsValid;         
        if (oldValue !== value)
        {
            model_internal::_messageIsValid = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "messageIsValid", oldValue, value));
        }                             
    }

    [Bindable(event="propertyChange")]
    public function get messageIsValid():Boolean
    {
        if (!model_internal::_messageIsValidCacheInitialized)
        {
            model_internal::calculateMessageIsValid();
        }

        return model_internal::_messageIsValid;
    }

    model_internal function calculateMessageIsValid():void
    {
        var valRes:ValidationResultEvent = model_internal::_messageValidator.validate(model_internal::_instance.message)
        model_internal::_messageIsValid_der = (valRes.results == null);
        model_internal::_messageIsValidCacheInitialized = true;
        if (valRes.results == null)
             model_internal::messageValidationFailureMessages_der = emptyArray;
        else
        {
            var _valFailures:Array = new Array();
            for (var a:int = 0 ; a<valRes.results.length ; a++)
            {
                _valFailures.push(valRes.results[a].errorMessage);
            }
            model_internal::messageValidationFailureMessages_der = _valFailures;
        }
    }

    [Bindable(event="propertyChange")]
    public function get messageValidationFailureMessages():Array
    {
        if (model_internal::_messageValidationFailureMessages == null)
            model_internal::calculateMessageIsValid();

        return _messageValidationFailureMessages;
    }

    model_internal function set messageValidationFailureMessages_der(value:Array) : void
    {
        var oldValue:Array = model_internal::_messageValidationFailureMessages;

        var needUpdate : Boolean = false;
        if (oldValue == null)
            needUpdate = true;
    
        // avoid firing the event when old and new value are different empty arrays
        if (!needUpdate && (oldValue !== value && (oldValue.length > 0 || value.length > 0)))
        {
            if (oldValue.length == value.length)
            {
                for (var a:int=0; a < oldValue.length; a++)
                {
                    if (oldValue[a] !== value[a])
                    {
                        needUpdate = true;
                        break;
                    }
                }
            }
            else
            {
                needUpdate = true;
            }
        }

        if (needUpdate)
        {
            model_internal::_messageValidationFailureMessages = value;   
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "messageValidationFailureMessages", oldValue, value));
            // Only execute calculateIsValid if it has been called before, to update the validationFailureMessages for
            // the entire entity.
            if (model_internal::_instance.model_internal::_cacheInitialized_isValid)
            {
                model_internal::_instance.model_internal::isValid_der = model_internal::_instance.model_internal::calculateIsValid();
            }
        }
    }

    [Bindable(event="propertyChange")]   
    public function get usersStyle():com.adobe.fiber.styles.Style
    {
        return model_internal::_nullStyle;
    }

    public function get usersValidator() : StyleValidator
    {
        return model_internal::_usersValidator;
    }

    model_internal function set _usersIsValid_der(value:Boolean):void 
    {
        var oldValue:Boolean = model_internal::_usersIsValid;         
        if (oldValue !== value)
        {
            model_internal::_usersIsValid = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "usersIsValid", oldValue, value));
        }                             
    }

    [Bindable(event="propertyChange")]
    public function get usersIsValid():Boolean
    {
        if (!model_internal::_usersIsValidCacheInitialized)
        {
            model_internal::calculateUsersIsValid();
        }

        return model_internal::_usersIsValid;
    }

    model_internal function calculateUsersIsValid():void
    {
        var valRes:ValidationResultEvent = model_internal::_usersValidator.validate(model_internal::_instance.users)
        model_internal::_usersIsValid_der = (valRes.results == null);
        model_internal::_usersIsValidCacheInitialized = true;
        if (valRes.results == null)
             model_internal::usersValidationFailureMessages_der = emptyArray;
        else
        {
            var _valFailures:Array = new Array();
            for (var a:int = 0 ; a<valRes.results.length ; a++)
            {
                _valFailures.push(valRes.results[a].errorMessage);
            }
            model_internal::usersValidationFailureMessages_der = _valFailures;
        }
    }

    [Bindable(event="propertyChange")]
    public function get usersValidationFailureMessages():Array
    {
        if (model_internal::_usersValidationFailureMessages == null)
            model_internal::calculateUsersIsValid();

        return _usersValidationFailureMessages;
    }

    model_internal function set usersValidationFailureMessages_der(value:Array) : void
    {
        var oldValue:Array = model_internal::_usersValidationFailureMessages;

        var needUpdate : Boolean = false;
        if (oldValue == null)
            needUpdate = true;
    
        // avoid firing the event when old and new value are different empty arrays
        if (!needUpdate && (oldValue !== value && (oldValue.length > 0 || value.length > 0)))
        {
            if (oldValue.length == value.length)
            {
                for (var a:int=0; a < oldValue.length; a++)
                {
                    if (oldValue[a] !== value[a])
                    {
                        needUpdate = true;
                        break;
                    }
                }
            }
            else
            {
                needUpdate = true;
            }
        }

        if (needUpdate)
        {
            model_internal::_usersValidationFailureMessages = value;   
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "usersValidationFailureMessages", oldValue, value));
            // Only execute calculateIsValid if it has been called before, to update the validationFailureMessages for
            // the entire entity.
            if (model_internal::_instance.model_internal::_cacheInitialized_isValid)
            {
                model_internal::_instance.model_internal::isValid_der = model_internal::_instance.model_internal::calculateIsValid();
            }
        }
    }

    [Bindable(event="propertyChange")]   
    public function get pages_totalStyle():com.adobe.fiber.styles.Style
    {
        return model_internal::_nullStyle;
    }

    public function get pages_totalValidator() : StyleValidator
    {
        return model_internal::_pages_totalValidator;
    }

    model_internal function set _pages_totalIsValid_der(value:Boolean):void 
    {
        var oldValue:Boolean = model_internal::_pages_totalIsValid;         
        if (oldValue !== value)
        {
            model_internal::_pages_totalIsValid = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "pages_totalIsValid", oldValue, value));
        }                             
    }

    [Bindable(event="propertyChange")]
    public function get pages_totalIsValid():Boolean
    {
        if (!model_internal::_pages_totalIsValidCacheInitialized)
        {
            model_internal::calculatePages_totalIsValid();
        }

        return model_internal::_pages_totalIsValid;
    }

    model_internal function calculatePages_totalIsValid():void
    {
        var valRes:ValidationResultEvent = model_internal::_pages_totalValidator.validate(model_internal::_instance.pages_total)
        model_internal::_pages_totalIsValid_der = (valRes.results == null);
        model_internal::_pages_totalIsValidCacheInitialized = true;
        if (valRes.results == null)
             model_internal::pages_totalValidationFailureMessages_der = emptyArray;
        else
        {
            var _valFailures:Array = new Array();
            for (var a:int = 0 ; a<valRes.results.length ; a++)
            {
                _valFailures.push(valRes.results[a].errorMessage);
            }
            model_internal::pages_totalValidationFailureMessages_der = _valFailures;
        }
    }

    [Bindable(event="propertyChange")]
    public function get pages_totalValidationFailureMessages():Array
    {
        if (model_internal::_pages_totalValidationFailureMessages == null)
            model_internal::calculatePages_totalIsValid();

        return _pages_totalValidationFailureMessages;
    }

    model_internal function set pages_totalValidationFailureMessages_der(value:Array) : void
    {
        var oldValue:Array = model_internal::_pages_totalValidationFailureMessages;

        var needUpdate : Boolean = false;
        if (oldValue == null)
            needUpdate = true;
    
        // avoid firing the event when old and new value are different empty arrays
        if (!needUpdate && (oldValue !== value && (oldValue.length > 0 || value.length > 0)))
        {
            if (oldValue.length == value.length)
            {
                for (var a:int=0; a < oldValue.length; a++)
                {
                    if (oldValue[a] !== value[a])
                    {
                        needUpdate = true;
                        break;
                    }
                }
            }
            else
            {
                needUpdate = true;
            }
        }

        if (needUpdate)
        {
            model_internal::_pages_totalValidationFailureMessages = value;   
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "pages_totalValidationFailureMessages", oldValue, value));
            // Only execute calculateIsValid if it has been called before, to update the validationFailureMessages for
            // the entire entity.
            if (model_internal::_instance.model_internal::_cacheInitialized_isValid)
            {
                model_internal::_instance.model_internal::isValid_der = model_internal::_instance.model_internal::calculateIsValid();
            }
        }
    }

    [Bindable(event="propertyChange")]   
    public function get successStyle():com.adobe.fiber.styles.Style
    {
        return model_internal::_nullStyle;
    }


     /**
     * 
     * @inheritDoc 
     */ 
     override public function getStyle(propertyName:String):com.adobe.fiber.styles.IStyle
     {
         switch(propertyName)
         {
            default:
            {
                return null;
            }
         }
     }
     
     /**
     * 
     * @inheritDoc 
     *  
     */  
     override public function getPropertyValidationFailureMessages(propertyName:String):Array
     {
         switch(propertyName)
         {
            case("message"):
            {
                return messageValidationFailureMessages;
            }
            case("users"):
            {
                return usersValidationFailureMessages;
            }
            case("pages_total"):
            {
                return pages_totalValidationFailureMessages;
            }
            default:
            {
                return emptyArray;
            }
         }
     }

}

}
