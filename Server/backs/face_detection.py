import cv2
import sys
import numpy as np
from PIL import Image

# 사진과 얼굴의 어떤 부분을 인식할까요?
imagePath = sys.argv[1]
cascPath = "haarcascade_frontalface_default.xml"
print(cv2.CascadeClassifier(cascPath))
# haar cascade를 결정합니다
faceCascade = cv2.CascadeClassifier(cascPath)

# 이미지를 읽어와 흑백사진으로 전환합니다
image = cv2.imread(imagePath)
i = Image.open(imagePath)
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

# 흑백사진 상태에서 얼굴을 탐지합니다
faces = faceCascade.detectMultiScale(
    gray,
    scaleFactor=1.1,
    minNeighbors=10,
    minSize=(30, 30)
    # flags = cv2.cv.CV_HAAR_SCALE_IMAGE
)

# 몇개의 얼굴을 찾았는지 말합니다
# 얼굴이 없는 사람들이 종종 있어요
print("Found {0} faces!".format(len(faces)))
if len(faces) > 1:
    print("얼굴2개이상감지. 추후 서버작업필")
elif len(faces) == 0:
    print("얼굴 안나옴. 추후 서버작업필")



width, height  = i.size
print(width, height)
f=open('size.txt','w')
f.write(str(width));
f.write(',');
f.write(str(height));
f.close()
# 얼굴에 초록색 사각형 그려주기 -> RGB (0, 255, 0)
# 빨강은 (255,0,0), 파랑은 (0,0,255)
for (x, y, w, h) in faces:
    cv2.rectangle(image, (x, y), (x+w, y+h), (0, 255, 0), 2)

print(faces);
np.savetxt('xy.txt',faces,fmt='%d',delimiter=",")
 #   cropped = image[y - int(h/1.9):y + h + int(h/1.5), x - int(w/4):x + w + int(w/4)]
   # cropped = image[y - int(h/1.66):y + h + int(h*1.23), x - int(w/4):x + w + int(w/4)]
    # 원으로 그린다면?
    # cv2.circle(image,(x+int(w/2),y+int(h/2)), w, (0,0,255), 2)

# 얼굴에 사각형을 친 사진을 출력합니다
cv2.imwrite("Faces.png", image)
# cv2.imwrite("Faces_circle.png", image)
cv2.waitKey(0)
