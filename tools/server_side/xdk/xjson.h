#ifndef XJSON_H_
#define XJSON_H_

/**
 * @author
 *  Santa Zhang
 *
 * @file
 *  xjson.h
 *
 * @brief
 *  Very simple json implementation.
 */

#include "xstr.h"

//  Implementation of json objects. Hidden from user.
struct xjson_impl;

/**
 * @brief
 *  Simple json implementation.
 */
typedef struct xjson_impl* xjson;

/**
 * @brief
 *  Create a new json object. It is "null" by default.
 *
 * @return
 *  New json object, initialized.
 */
xjson xjson_new();

/**
 * @brief
 *  Destroy a json object.
 *
 * @param xj
 *  The json object to be destroyed.
 */
void xjson_delete(xjson xj);

/**
 * @brief
 *  Set an json object to c-string value.
 *
 * @param xj
 *  The json object.
 * @param cs
 *  The c-string value.
 */
void xjson_set_cstr(xjson xj, const char* cs);

/**
 * @brief
 *  Set an json object to string value.
 *
 * @param xj
 *  The json object.
 * @param xs
 *  The string value.
 */
void xjson_set_xstr(xjson xj, xstr xs);

/**
 * @brief
 *  Set an json object to int value.
 *
 * @param xj
 *  The json object.
 * @param val
 *  The int value.
 */
void xjson_set_int(xjson xj, int val);

/**
 * @brief
 *  Set an json object to double value.
 *
 * @param xj
 *  The json object.
 * @param val
 *  The double value.
 */
void xjson_set_double(xjson xj, double val);

/**
 * @brief
 *  Set an json object to null.
 *
 * @param xj
 *  The json object.
 */
void xjson_set_null(xjson xj);

/**
 * @brief
 *  Set an json object to true.
 *
 * @param xj
 *  The json object.
 */
void xjson_set_true(xjson xj);

/**
 * @brief
 *  Set an json object to false.
 *
 * @param xj
 *  The json object.
 */
void xjson_set_false(xjson xj);

/**
 * @brief
 *  Set an json object as object.
 *
 * @param xj
 *  The json object.
 */
void xjson_set_object(xjson xj);

/**
 * @brief
 *  Set an json object as array.
 *
 * @param xj
 *  The json object.
 */
void xjson_set_array(xjson xj);

/**
 * @brief
 *  Add a new element into the array.
 *
 * @param xj_array
 *  The array.
 * @param xj_elem
 *  The new element.
 *
 * @return
 *  XSUCCESS if successful. If xj_obj is not set to array, then XFAILURE will be returned.
 */
xsuccess xjson_array_append(xjson xj_array, xjson xj_elem);

/**
 * @brief
 *  Add a new attribute into the object.
 *
 * @param xj_obj
 *  The object.
 * @param key
 *  The key of attribute. It will be copied for internal memory management.
 * @param xj_attr
 *  The attribute.
 *
 * @return
 *  XSUCCESS if successful. If xj_obj is not set to object, then XFAILURE will be returned.
 */
xsuccess xjson_object_put(xjson xj_obj, const char* key, xjson xj_attr);

/**
 * @brief
 *  Convert the json object to string.
 *
 * @param xj
 *  The json object.
 * @param xs
 *  The xstr to hold the json object's text representation.
 */
void xjson_to_xstr(xjson xj, xstr xs);

#endif  // XJSON_H_

