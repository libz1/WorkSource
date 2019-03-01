#!/usr/bin/python
# -*- coding: utf-8 -*-

import cv2  
import numpy
import matplotlib.pyplot as plot
import datetime
import time

def get_time_stamp():
    ct = time.time()
    local_time = time.localtime(ct)
    data_head = time.strftime("%Y-%m-%d %H-%M-%S", local_time)
    data_secs = (ct - long(ct)) * 1000
    time_stamp = "%s-%03d" % (data_head, data_secs)
    return time_stamp

cap = cv2.VideoCapture(0)

starttime = datetime.datetime.now()
endtime = datetime.datetime.now()
(endtime - starttime).seconds

while(1):
    # get a frame
    ret, frame = cap.read()
    # show a frame
    nowTime=datetime.datetime.now().strftime('%Y-%m-%d %H-%M-%S')
    print(nowTime)
    cv2.imwrite(nowTime+'.jpg',frame,[int(cv2.IMWRITE_JPEG_QUALITY),40])
    cv2.imshow("capture", frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break
cap.release()
cv2.destroyAllWindows() 
