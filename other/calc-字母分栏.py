#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys
import xlwt

wbk = xlwt.Workbook()
sheet1 = wbk.add_sheet('sheet 1',cell_overwrite_ok=True)


f = open("d:\\a.txt","r")
line = f.readline()

s = "abcdefghijklmnopqrstuvwxyz"
s_array = []
s_array_i = []


for letter in s: 
    s_array.append(letter )
    s_array_i.append(1)

while line:
    if "Module" in line:
       print(line)       
    else:
        for index in range(len(s_array)):
            i = s_array_i[index]
            if line[0:1] == s_array[index]:              
                sheet1.write(i,index,line)#第i行第index列写入内容
                s_array_i[index] = s_array_i[index] + 1
                sheet1.write(0,index,i)#第i行第index列写入内容
    line = f.readline()

sheet1.write(0, 26, xlwt.Formula('SUM(A1:Z1)'))
f.close
wbk.save('test1.xls')
