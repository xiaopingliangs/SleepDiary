package app.sleepdiary.offline.application

import app.sleepdiary.offline.data.dao.AssessmentWithAnswers
import app.sleepdiary.offline.data.entity.*
import app.sleepdiary.offline.domain.model.*
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.time.LocalDate

/** Structural decoder for archived RAW_INPUT_CAPTURE_1. Does not reevaluate historical medical facts. */
internal object RawInputDecoder {
    fun verify(bytes: ByteArray, from: LocalDate, through: LocalDate) {
        val input=DataInputStream(ByteArrayInputStream(bytes))
        fun flag():Boolean { val value=input.readUnsignedByte();require(value in 0..1);return value==1 }
        fun text():String? {
            if(!flag()) return null
            val count=input.readInt();require(count>=0 && count<=input.available())
            val data=ByteArray(count);input.readFully(data)
            return Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(data)).toString()
        }
        fun required()=checkNotNull(text())
        fun count():Int = input.readInt().also { require(it>=0 && it<=input.available()) }
        require(required()==RawInputEncoding.VERSION)
        require(required()==from.toString() && required()==through.toString())
        val entries=List(count()) {
            SleepDiaryEntry(required(),required(),required(),required(),input.readInt(),input.readInt(),input.readInt(),
                required(),required(),input.readInt(),text(),required(),required(),required(),required(),flag())
        }
        val safety=List(count()) {
            val a=SafetyAssessment(required(),required(),required(),SafetyGate.valueOf(required()),required(),
                AssessmentStatus.valueOf(required()),flag())
            val answers=List(count()) { SafetyAnswer(required(),required(),required(),required(),text()) }
            AssessmentWithAnswers(a,answers)
        }
        val state=if(flag()) AppState(input.readInt(),required(),text(),flag(),flag(),input.readInt(),text(),flag()) else null
        require(input.available()==0)
        // Also rejects duplicate IDs, out-of-range rows, unsorted arrays and malformed UTF-8.
        require(RawInputEncoding.encode(from,through,entries,safety,state).contentEquals(bytes))
    }
}
