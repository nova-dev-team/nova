/**
 * This is a generated class and is not intended for modification.  To customize behavior
 * of this value object you may modify the generated sub-class of this class - UserListRep.as.
 */

package valueObjects
{
import com.adobe.fiber.services.IFiberManagingService;
import com.adobe.fiber.util.FiberUtils;
import com.adobe.fiber.valueobjects.IValueObject;
import flash.events.Event;
import flash.events.EventDispatcher;
import mx.binding.utils.ChangeWatcher;
import mx.collections.ArrayCollection;
import mx.events.CollectionEvent;
import mx.events.PropertyChangeEvent;
import mx.validators.ValidationResult;
import valueObjects.Users_type;

import flash.net.registerClassAlias;
import flash.net.getClassByAlias;
import com.adobe.fiber.core.model_internal;
import com.adobe.fiber.valueobjects.IPropertyIterator;
import com.adobe.fiber.valueobjects.AvailablePropertyIterator;

use namespace model_internal;

[ExcludeClass]
public class _Super_UserListRep extends flash.events.EventDispatcher implements com.adobe.fiber.valueobjects.IValueObject
{
    model_internal static function initRemoteClassAliasSingle(cz:Class) : void
    {
    }

    model_internal static function initRemoteClassAliasAllRelated() : void
    {
        valueObjects.Users_type.initRemoteClassAliasSingleChild();
    }

    model_internal var _dminternal_model : _UserListRepEntityMetadata;

    /**
     * properties
     */
    private var _internal_message : String;
    private var _internal_users : ArrayCollection;
    model_internal var _internal_users_leaf:valueObjects.Users_type;
    private var _internal_pages_total : Object;
    private var _internal_success : Boolean;

    private static var emptyArray:Array = new Array();


    /**
     * derived property cache initialization
     */
    model_internal var _cacheInitialized_isValid:Boolean = false;

    model_internal var _changeWatcherArray:Array = new Array();

    public function _Super_UserListRep()
    {
        _model = new _UserListRepEntityMetadata(this);

        // Bind to own data properties for cache invalidation triggering
        model_internal::_changeWatcherArray.push(mx.binding.utils.ChangeWatcher.watch(this, "message", model_internal::setterListenerMessage));
        model_internal::_changeWatcherArray.push(mx.binding.utils.ChangeWatcher.watch(this, "users", model_internal::setterListenerUsers));
        model_internal::_changeWatcherArray.push(mx.binding.utils.ChangeWatcher.watch(this, "pages_total", model_internal::setterListenerPages_total));

    }

    /**
     * data property getters
     */

    [Bindable(event="propertyChange")]
    public function get message() : String
    {
        return _internal_message;
    }

    [Bindable(event="propertyChange")]
    public function get users() : ArrayCollection
    {
        return _internal_users;
    }

    [Bindable(event="propertyChange")]
    public function get pages_total() : Object
    {
        return _internal_pages_total;
    }

    [Bindable(event="propertyChange")]
    public function get success() : Boolean
    {
        return _internal_success;
    }

    /**
     * data property setters
     */

    public function set message(value:String) : void
    {
        var oldValue:String = _internal_message;
        if (oldValue !== value)
        {
            _internal_message = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "message", oldValue, _internal_message));
        }
    }

    public function set users(value:*) : void
    {
        var oldValue:ArrayCollection = _internal_users;
        if (oldValue !== value)
        {
            if (value is ArrayCollection)
            {
                _internal_users = value;
            }
            else if (value is Array)
            {
                _internal_users = new ArrayCollection(value);
            }
            else
            {
                throw new Error("value of users must be a collection");
            }
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "users", oldValue, _internal_users));
        }
    }

    public function set pages_total(value:Object) : void
    {
        var oldValue:Object = _internal_pages_total;
        if (oldValue !== value)
        {
            _internal_pages_total = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "pages_total", oldValue, _internal_pages_total));
        }
    }

    public function set success(value:Boolean) : void
    {
        var oldValue:Boolean = _internal_success;
        if (oldValue !== value)
        {
            _internal_success = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "success", oldValue, _internal_success));
        }
    }

    /**
     * Data property setter listeners
     *
     * Each data property whose value affects other properties or the validity of the entity
     * needs to invalidate all previously calculated artifacts. These include:
     *  - any derived properties or constraints that reference the given data property.
     *  - any availability guards (variant expressions) that reference the given data property.
     *  - any style validations, message tokens or guards that reference the given data property.
     *  - the validity of the property (and the containing entity) if the given data property has a length restriction.
     *  - the validity of the property (and the containing entity) if the given data property is required.
     */

    model_internal function setterListenerMessage(value:flash.events.Event):void
    {
        _model.invalidateDependentOnMessage();
    }

    model_internal function setterListenerUsers(value:flash.events.Event):void
    {
        if (value is mx.events.PropertyChangeEvent)
        {
            if (mx.events.PropertyChangeEvent(value).newValue)
            {
                mx.events.PropertyChangeEvent(value).newValue.addEventListener(mx.events.CollectionEvent.COLLECTION_CHANGE, model_internal::setterListenerUsers);
            }
        }
        _model.invalidateDependentOnUsers();
    }

    model_internal function setterListenerPages_total(value:flash.events.Event):void
    {
        _model.invalidateDependentOnPages_total();
    }


    /**
     * valid related derived properties
     */
    model_internal var _isValid : Boolean;
    model_internal var _invalidConstraints:Array = new Array();
    model_internal var _validationFailureMessages:Array = new Array();

    /**
     * derived property calculators
     */

    /**
     * isValid calculator
     */
    model_internal function calculateIsValid():Boolean
    {
        var violatedConsts:Array = new Array();
        var validationFailureMessages:Array = new Array();

        var propertyValidity:Boolean = true;
        if (!_model.messageIsValid)
        {
            propertyValidity = false;
            com.adobe.fiber.util.FiberUtils.arrayAdd(validationFailureMessages, _model.model_internal::_messageValidationFailureMessages);
        }
        if (!_model.usersIsValid)
        {
            propertyValidity = false;
            com.adobe.fiber.util.FiberUtils.arrayAdd(validationFailureMessages, _model.model_internal::_usersValidationFailureMessages);
        }
        if (!_model.pages_totalIsValid)
        {
            propertyValidity = false;
            com.adobe.fiber.util.FiberUtils.arrayAdd(validationFailureMessages, _model.model_internal::_pages_totalValidationFailureMessages);
        }

        model_internal::_cacheInitialized_isValid = true;
        model_internal::invalidConstraints_der = violatedConsts;
        model_internal::validationFailureMessages_der = validationFailureMessages;
        return violatedConsts.length == 0 && propertyValidity;
    }

    /**
     * derived property setters
     */

    model_internal function set isValid_der(value:Boolean) : void
    {
        var oldValue:Boolean = model_internal::_isValid;
        if (oldValue !== value)
        {
            model_internal::_isValid = value;
            _model.model_internal::fireChangeEvent("isValid", oldValue, model_internal::_isValid);
        }
    }

    /**
     * derived property getters
     */

    [Transient]
    [Bindable(event="propertyChange")]
    public function get _model() : _UserListRepEntityMetadata
    {
        return model_internal::_dminternal_model;
    }

    public function set _model(value : _UserListRepEntityMetadata) : void
    {
        var oldValue : _UserListRepEntityMetadata = model_internal::_dminternal_model;
        if (oldValue !== value)
        {
            model_internal::_dminternal_model = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "_model", oldValue, model_internal::_dminternal_model));
        }
    }

    /**
     * methods
     */


    /**
     *  services
     */
    private var _managingService:com.adobe.fiber.services.IFiberManagingService;

    public function set managingService(managingService:com.adobe.fiber.services.IFiberManagingService):void
    {
        _managingService = managingService;
    }

    model_internal function set invalidConstraints_der(value:Array) : void
    {
        var oldValue:Array = model_internal::_invalidConstraints;
        // avoid firing the event when old and new value are different empty arrays
        if (oldValue !== value && (oldValue.length > 0 || value.length > 0))
        {
            model_internal::_invalidConstraints = value;
            _model.model_internal::fireChangeEvent("invalidConstraints", oldValue, model_internal::_invalidConstraints);
        }
    }

    model_internal function set validationFailureMessages_der(value:Array) : void
    {
        var oldValue:Array = model_internal::_validationFailureMessages;
        // avoid firing the event when old and new value are different empty arrays
        if (oldValue !== value && (oldValue.length > 0 || value.length > 0))
        {
            model_internal::_validationFailureMessages = value;
            _model.model_internal::fireChangeEvent("validationFailureMessages", oldValue, model_internal::_validationFailureMessages);
        }
    }

    model_internal var _doValidationCacheOfMessage : Array = null;
    model_internal var _doValidationLastValOfMessage : String;

    model_internal function _doValidationForMessage(valueIn:Object):Array
    {
        var value : String = valueIn as String;

        if (model_internal::_doValidationCacheOfMessage != null && model_internal::_doValidationLastValOfMessage == value)
           return model_internal::_doValidationCacheOfMessage ;

        _model.model_internal::_messageIsValidCacheInitialized = true;
        var validationFailures:Array = new Array();
        var errorMessage:String;
        var failure:Boolean;

        var valRes:ValidationResult;
        if (_model.isMessageAvailable && _internal_message == null)
        {
            validationFailures.push(new ValidationResult(true, "", "", "message is required"));
        }

        model_internal::_doValidationCacheOfMessage = validationFailures;
        model_internal::_doValidationLastValOfMessage = value;

        return validationFailures;
    }
    
    model_internal var _doValidationCacheOfUsers : Array = null;
    model_internal var _doValidationLastValOfUsers : ArrayCollection;

    model_internal function _doValidationForUsers(valueIn:Object):Array
    {
        var value : ArrayCollection = valueIn as ArrayCollection;

        if (model_internal::_doValidationCacheOfUsers != null && model_internal::_doValidationLastValOfUsers == value)
           return model_internal::_doValidationCacheOfUsers ;

        _model.model_internal::_usersIsValidCacheInitialized = true;
        var validationFailures:Array = new Array();
        var errorMessage:String;
        var failure:Boolean;

        var valRes:ValidationResult;
        if (_model.isUsersAvailable && _internal_users == null)
        {
            validationFailures.push(new ValidationResult(true, "", "", "users is required"));
        }

        model_internal::_doValidationCacheOfUsers = validationFailures;
        model_internal::_doValidationLastValOfUsers = value;

        return validationFailures;
    }
    
    model_internal var _doValidationCacheOfPages_total : Array = null;
    model_internal var _doValidationLastValOfPages_total : Object;

    model_internal function _doValidationForPages_total(valueIn:Object):Array
    {
        var value : Object = valueIn as Object;

        if (model_internal::_doValidationCacheOfPages_total != null && model_internal::_doValidationLastValOfPages_total == value)
           return model_internal::_doValidationCacheOfPages_total ;

        _model.model_internal::_pages_totalIsValidCacheInitialized = true;
        var validationFailures:Array = new Array();
        var errorMessage:String;
        var failure:Boolean;

        var valRes:ValidationResult;
        if (_model.isPages_totalAvailable && _internal_pages_total == null)
        {
            validationFailures.push(new ValidationResult(true, "", "", "pages_total is required"));
        }

        model_internal::_doValidationCacheOfPages_total = validationFailures;
        model_internal::_doValidationLastValOfPages_total = value;

        return validationFailures;
    }
    

}

}
