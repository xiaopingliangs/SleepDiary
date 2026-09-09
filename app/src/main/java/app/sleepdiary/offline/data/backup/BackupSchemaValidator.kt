package app.sleepdiary.offline.data.backup

import android.util.JsonReader
import android.util.JsonToken
import org.json.*
import java.io.StringReader
import java.time.*
import java.util.UUID

/** Implements the assertions used by the bundled, versioned schemas; no remote resolution. */
object BackupSchemaValidator {
 fun parse(text:String):JSONObject {
  JsonReader(StringReader(text)).use { r ->
   r.isLenient=false
   fun value():Any = when(r.peek()) {
    JsonToken.BEGIN_OBJECT -> {val o=JSONObject();r.beginObject();while(r.hasNext()){val k=r.nextName();require(!o.has(k)){"重复 JSON 键"};o.put(k,value())};r.endObject();o}
    JsonToken.BEGIN_ARRAY -> {val a=JSONArray();r.beginArray();while(r.hasNext())a.put(value());r.endArray();a}
    JsonToken.STRING -> r.nextString()
    JsonToken.NUMBER -> {val n=r.nextString();n.toLongOrNull() ?: n.toDouble().also {require(it.isFinite())}}
    JsonToken.BOOLEAN -> r.nextBoolean()
    JsonToken.NULL -> {r.nextNull();JSONObject.NULL}
    else -> error("JSON 内容损坏")
   }
   val result=value();require(r.peek()==JsonToken.END_DOCUMENT);return result as JSONObject
  }
 }
 fun validate(value:Any?,schema:JSONObject) {
  fun matches(s:JSONObject)=runCatching {validate(value,s)}.isSuccess
  schema.optJSONArray("allOf")?.let {for(i in 0 until it.length())validate(value,it.getJSONObject(i))}
  schema.optJSONArray("oneOf")?.let {a->require((0 until a.length()).count{matches(a.getJSONObject(it))}==1)}
  schema.optJSONObject("if")?.let {if(matches(it))schema.optJSONObject("then")?.let {s->validate(value,s)}}
  fun equal(a:Any?,b:Any?)=if(a is Number && b is Number)a.toDouble()==b.toDouble() else a==b
  if(schema.has("const"))require(equal(value,schema.get("const")))
  schema.optJSONArray("enum")?.let {a->require((0 until a.length()).any{equal(value,a.get(it))})}
  fun type(t:String)=when(t){"null"->value==null||value===JSONObject.NULL;"object"->value is JSONObject;"array"->value is JSONArray;"string"->value is String;"boolean"->value is Boolean;"number"->value is Number;"integer"->value is Number && value.toDouble()%1.0==0.0;else->error("Unsupported schema type")}
  if(schema.has("type")){val t=schema.get("type");require(if(t is JSONArray)(0 until t.length()).any{type(t.getString(it))}else type(t as String))}
  when(value){
   is JSONObject -> {
    schema.optJSONArray("required")?.let {a->for(i in 0 until a.length())require(value.has(a.getString(i)))}
    val props=schema.optJSONObject("properties")?:JSONObject()
    value.keys().forEach {k->if(props.has(k))validate(value.get(k),props.getJSONObject(k))else require(schema.opt("additionalProperties")!=false)}
   }
   is JSONArray -> {
    require(value.length()>=schema.optInt("minItems",0))
    if(schema.optBoolean("uniqueItems"))require((0 until value.length()).map{value.get(it).toString()}.distinct().size==value.length())
    schema.optJSONObject("items")?.let {s->for(i in 0 until value.length())validate(value.get(i),s)}
   }
   is Number -> {require(value.toDouble().isFinite());if(schema.has("minimum"))require(value.toDouble()>=schema.getDouble("minimum"));if(schema.has("maximum"))require(value.toDouble()<=schema.getDouble("maximum"))}
   is String -> {
    require(value.codePointCount(0,value.length)>=schema.optInt("minLength",0))
    if(schema.has("pattern"))require(Regex(schema.getString("pattern")).containsMatchIn(value))
    when(schema.optString("format")){"uuid"->require(UUID.fromString(value).toString()==value.lowercase());"date"->LocalDate.parse(value);"date-time"->OffsetDateTime.parse(value)}
   }
  }
 }
}
