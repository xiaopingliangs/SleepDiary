package app.sleepdiary.offline.ui.diary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

/** FormState owns text; the standard Compose editor manages IME composition and selection. */
@Composable
fun CommentTextInput(value:String,onChange:(String)->Unit,label:String,tag:String,enabled:Boolean=true) {
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Text(label,style=MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value=value,onValueChange=onChange,enabled=enabled,
            minLines=2,maxLines=4,
            modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag(tag).semantics{contentDescription=label}
        )
    }
}
