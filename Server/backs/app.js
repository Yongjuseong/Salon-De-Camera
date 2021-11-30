const http=require('http');
const express=require('express');
const app=express();

app.set('routes',__dirname+'/routes');

const test=require('./routes/test.js');
const trans=require('./routes/trans.js')

app.use('/test',test);
app.use('/trans',trans);

app.listen(51234,()=>{
	console.log('Running at 51234...');
});

