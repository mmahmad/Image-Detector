from socket import *
import time
import StringIO
from PIL import Image
import base64


def decode_base64(data):
    """Decode base64, padding being optional.

    :param data: Base64 data as an ASCII byte string
    :returns: The decoded byte string.

    """
    missing_padding = 4 - len(data) % 4
    if missing_padding:
        data += b'='* missing_padding
    return base64.b64decode(data)


host = "0.0.0.0"

serverSocket=socket(AF_INET,SOCK_STREAM)

serverSocket.bind((host,10000))
totalLength=""
serverSocket.listen(10000)

print "Ready to serve"

data = []
message = ""

##connectionSocket,clientAddress=serverSocket.accept()
##message =connectionSocket.recv(4096)
##connectionSocket.close()
##length = message
##print length

connectionSocket,clientAddress=serverSocket.accept()
print clientAddress[1]

while 1:
        try:
                message =connectionSocket.recv(16384)
                data.append(message)
                if len(message) == 0:
                        print "Ended transmission"
                        break
##                print "No breaks"
##                print len(message)
        except:
                print "Breaks"
                break

print "Transmitted"
connectionSocket.send("BLAH")

connectionSocket.close()


serverSocket.close()

s = socket(AF_INET,SOCK_STREAM)


s.connect((clientAddress[0],50000))
s.send("frog")
s.close()



##print len(data)
fh = open("imageToSave.jpg", "wb")
##dS = decode_base64(data)
##fh.write(dS)
##fh.close()

for x in range(len(data)):
    fh.write(data[x])
fh.close()
    
print "Done"




