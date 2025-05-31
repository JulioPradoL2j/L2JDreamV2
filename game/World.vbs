' Get Java path.
Dim path
Set shell = WScript.CreateObject("WScript.Shell")
path = shell.Environment.Item("JAVA_HOME")
If path = "" Then
    MsgBox "Could not find JAVA_HOME environment variable!", vbOKOnly, "Game Server"
Else
    If InStr(path, "\bin") = 0 Then
        path = path + "\bin\"
    Else
        path = path + "\"
    End If
    path = Replace(path, "\\", "\")
    path = Replace(path, "Program Files", "Progra~1")
End If



' Generate command.
Dim command
command = path & "java -Xmx1280m -cp ../libs/*; com.dream.game.L2GameServer"

' Run the server.
Dim exitcode
exitcode = 0
Do
    ' Run the command and keep the console open.
    exitcode = shell.Run("cmd /c " & command & " & exit", 0, True)

    ' Handle the exit code
    If exitcode = 2 Then
        ' Restart
        exitcode = 2
    ElseIf exitcode <> 0 Then
        ' Error
       exitcode = 0
        Exit Do
    End If
Loop While exitcode = 2
