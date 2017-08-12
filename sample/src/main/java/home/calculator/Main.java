/**********************************
 * This software is created by Istiaq Ahmed.
 * for any kind of help
 * email: istiaqahmed90@gmail.com
 * Mobile : +8801814 160 320
 * country: Bangladesh
 * University: AIUB
 * 
 * 
 **********************************/

package home.calculator;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Main extends Activity 
{
    EditText editText;
    
    Button button0,button1,button2,button3,
                button4,button5,button6,button7,button8,button9,buttonPlus,buttonMinus,buttonMultiply,
                buttonDivide,buttonEqual,buttonPoint,buttonDel,buttonReset,
                button_sin,button_cos,button_tan,button_squared_2,button_root,button_del,button_dec,
                button_bin,button_pi;
    
    String sum="",one,oneAgain="",two,twoAgain="",three,threeAgain="",four,fourAgain="",five,fiveAgain="",
                            six,sixAgain,seven,sevenAgain="",eight,eightAgain="",nine,nineAgain="",
                            zero,plus,minus,multiply,divide,equal,point,del,reset,
                            dec_string="",hex_string="",oct_string="",pi="3.1416";
    
    Integer countOne=0,dec_num,unicode_value;
    
    Float result=0f,result_mul=1f,result_div=1f;
    
    int pressCount=1,sumZero,dec_flag=0,c,i;

    char press;
    
    String EditTextMsg,bin_num,hex_num,oct_num;
    
    Float floatEditTextMsg;
    
    Double doubleEditTextMsg,afterSin,after_cos,after_tan,toRadian_doubleEditTextMsg,after_squared_2,after_root,after_qube;
    
    Vibrator vibrator;
       
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        editText=(EditText)findViewById(R.id.editText1);

        button0=(Button)findViewById(R.id.button0);
        button1=(Button)findViewById(R.id.button1);
        button2=(Button)findViewById(R.id.button2);
        button3=(Button)findViewById(R.id.button3);
        button4=(Button)findViewById(R.id.button4);
        
        button5=(Button)findViewById(R.id.button5);
        button6=(Button)findViewById(R.id.button6);
        button7=(Button)findViewById(R.id.button7);
        button8=(Button)findViewById(R.id.button8);
        button9=(Button)findViewById(R.id.button9);
        
        buttonPlus=(Button)findViewById(R.id.buttonPlus);
        buttonMinus=(Button)findViewById(R.id.buttonMinus);
        buttonMultiply=(Button)findViewById(R.id.buttonMultiply);
        buttonDivide=(Button)findViewById(R.id.buttonDivide);
        buttonPoint=(Button)findViewById(R.id.buttonPoint);
        
        buttonEqual=(Button)findViewById(R.id.buttonEqual);
        
        button_sin=(Button)findViewById(R.id.button_sin);
        button_cos=(Button)findViewById(R.id.button_cos);
        button_tan=(Button)findViewById(R.id.button_tan);
        button_root=(Button)findViewById(R.id.button_root);
        button_squared_2=(Button)findViewById(R.id.button_squared_2);
        
        button_del=(Button)findViewById(R.id.button_del);
        button_dec=(Button)findViewById(R.id.button_dec);
        button_bin=(Button)findViewById(R.id.button_bin);
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); 
        //button_del=(Button)findViewById(R.id.button_del);
        
        editText.setText(result.toString());
    }
    
    public void onClickListener0(View v)
    {
        vibrator.vibrate(30);
        if(press=='=')
        {
            onClickListenerReset(buttonReset);
        }
        
        if(sum !="")
        {
            zero=(String)button0.getText();
            
            sum=sum+zero;
            editText.setText(sum);
        }
        else
        {
            sum="0";
            editText.setText("0");
        }
    }
    
    public void onClickListener1(View v)
    {
        vibrator.vibrate(30);
        
        if(press=='=')
        {
            onClickListenerReset(buttonReset);
        }
       
        one=(String)button1.getText();
        sum=sum+one;
        
        editText.setText(sum);
    }
    
    public void onClickListener2(View v)
    {
        vibrator.vibrate(30);
        
        if(press=='=')
        {
            onClickListenerReset(buttonReset);
        }
        
        if(press=='=')
        {
            onClickListenerEqual(buttonEqual);
        }
        
        two=(String)button2.getText();
        sum=sum+two;
        
        editText.setText(sum);
    }
    
    public void onClickListener3(View v)
    {
        vibrator.vibrate(30);
        if(press=='=')
        {
            onClickListenerReset(buttonReset);
        }
        three=(String)button3.getText();
        sum=sum+three;
        
        editText.setText(sum);
    }
    
    public void onClickListener4(View v)
    {
        vibrator.vibrate(30);
        
        if(press=='=')
        {
            onClickListenerReset(buttonReset);
        }
        
        four=(String)button4.getText();
        sum=sum+four;
        
        editText.setText(sum);
    }
    
    public void onClickListener5(View v)
    {
        vibrator.vibrate(30);
        if(press=='=')
        {
            onClickListenerReset(buttonReset);
        }
        
        five=(String)button5.getText();
        sum=sum+five;
        
        editText.setText(sum);
    }
    
    public void onClickListener6(View v)
    {
        vibrator.vibrate(30);
        if(press=='=')
        {
            onClickListenerReset(buttonReset);
        }
        
        six=(String)button6.getText();
        sum=sum+six;
        
        editText.setText(sum);
    }
    
    public void onClickListener7(View v)
    {
        vibrator.vibrate(30);
        if(press=='=')
        {
            onClickListenerReset(buttonReset);
        }
        
        seven=(String)button7.getText();
        sum=sum+seven;
        
        editText.setText(sum);
    }
    
    public void onClickListener8(View v)
    {
        vibrator.vibrate(30);
        if(press=='=')
        {
            onClickListenerReset(buttonReset);
        }
        
        eight=(String)button8.getText();
        sum=sum+eight;
        
        editText.setText(sum);
    }
    
    public void onClickListener9(View v)
    {
        vibrator.vibrate(30);
        if(press=='=')
        {
            onClickListenerReset(buttonReset);
        }
        
        nine=(String)button9.getText();
        sum=sum+nine;
        
        editText.setText(sum);
    }
    
    public void onClickListenerPlus(View v)
    {
        vibrator.vibrate(30);
        
        if(press=='-')
        {
            onClickListenerEqual(buttonMinus);
        }
        else if(press=='*')
        {
            onClickListenerEqual(buttonMultiply);
        }
        
        else if(press=='/')
        {
            onClickListenerEqual(buttonDivide);
        }
        
        
        press='*';
        
        if(sum != "")
        {
            result=result+Float.parseFloat(editText.getText().toString());
            
            editText.setText(result.toString());
            
            result_mul=result;
            
            result_div=result;
            
            sum="";            
        }
        else
        {
            editText.setText(result.toString());
            
            result_mul=result;
            
            result_div=result;
            
            sum="";
        }
        
    }
    
    public void onClickListenerMinus(View v)
    {
        vibrator.vibrate(30);
        if(press=='+')
        {
            onClickListenerEqual(buttonPlus);
        }
        else if(press=='*')
        {
            onClickListenerEqual(buttonMultiply);
        }
        else if(press=='/')
        {
            onClickListenerEqual(buttonDivide);
        }
       
        
        
        press='-';
        
        EditTextMsg= editText.getText().toString(); 
        floatEditTextMsg=Float.parseFloat(EditTextMsg);
        
        if(sum==""  && result==0)
        {
            sum=sum+'-';
            //Log.d("sum=","minus press");
        }
        else if(sum != "")
        {
            if(result==0)
            {
                result=Float.parseFloat(sum)-result;
                
                editText.setText(result.toString());
                
                result_mul=result;
                
                result_div=result;
                
                sum="";
            }
            
            else
            {
                result=result-Float.parseFloat(sum);
                
                editText.setText(result.toString());
                
                result_mul=result;
                
                result_div=result;
                
                sum="";
            }
        }
        
    }
    
    public void onClickListenerMultiply(View v)
    {
        vibrator.vibrate(30);
        if(press=='/')
        {
            onClickListenerEqual(buttonDivide);
        }
        else if(press=='+')
        {
            onClickListenerEqual(buttonPlus);
        }
        
        else if(press=='-')
        {
            onClickListenerEqual(buttonMinus);
        }
        
        
        
        press='*';
        
        EditTextMsg= editText.getText().toString(); 
        floatEditTextMsg=Float.parseFloat(EditTextMsg);
        
        if(sum !="")
        {
            result_mul=result_mul * floatEditTextMsg; 
            
            result=result_mul;
            
            result_div=result_mul;
            
            editText.setText(result_mul.toString());
            
            sum="";
        }
        else
        {
            editText.setText(EditTextMsg);
            
            //result_mul=result_mul * Float.parseFloat(sum);
            
            //result=result_mul;
            
            sum="";
        }
        
    }
    
    
    public void onClickListenerDivide(View v)
    {
        vibrator.vibrate(30);
        if(press=='+')
        {
            onClickListenerEqual(buttonPlus);
        }
        
        else if(press=='-')
        {
            onClickListenerEqual(buttonMinus);
        }
        
        else if(press=='*')
        {
            onClickListenerEqual(buttonMultiply);
        }
        
        press='/';
        
        EditTextMsg= editText.getText().toString(); 
        floatEditTextMsg=Float.parseFloat(EditTextMsg);
        
        if(sum !="" && result_div==1)
        {
            //int c=0;
            
            if(c==0)
            {
                result_div=floatEditTextMsg/result_div;  
                Log.d("if if result_div=", result_div.toString());
                c++;
            }
            else
            {
                result_div=result_div/floatEditTextMsg; 
                Log.d("if else result_div=", result_div.toString());
            }
            
            
            result=result_div;
            result_mul=result_div;
            
            editText.setText(result_div.toString());
            
            sum="";
        }
        else if(sum !="" && result_div !=1)
        {
            result_div=result_div/floatEditTextMsg; 
            
            Log.d("else if result_div=", result_div.toString());
            
            result=result_div;
            
            result_mul=result_div;
            
            editText.setText(result_div.toString());
            
            sum="";
        }
        else
        {
            editText.setText(EditTextMsg);
            
            sum="";
        }
    }
    
    
    
    public void onClickListenerPoint(View v)
    {
        vibrator.vibrate(30);
        
       int error=0;
        
        if(sum !=null)
        {
            for(int i=0;i<sum.length();i++)
            {
                if(sum.charAt(i)=='.')
                {
                    error=1;
                    break;
                }
            }

        }
        
        if(error==0)
        {
            if(sum==null)
            {
                sum=sum+"0.";
            }
            else
            {
                sum=sum+".";
            }
        }
       
        editText.setText(sum);
    }
    
    public void onClickListenerEqual(View v)
    {
        vibrator.vibrate(30);
       
        if(press=='+') 
        {
            onClickListenerPlus(buttonPlus);
            //msg1= editText.getText().toString(); 
            //floatMsg=Float.parseFloat(msg1);
        }
        else if(press=='-') 
        {
            onClickListenerMinus(buttonMinus);
        }
        else if(press=='*') 
        {
            onClickListenerMultiply(buttonMultiply);
        }
        else if(press=='/')
        {
            onClickListenerDivide(buttonDivide);
        }
        
        press='=';
        
        
    }
    
    public void onClickListenerExit(View v)
    {
        vibrator.vibrate(30);
        finish();
    }
    
    public void onClickListenerReset(View v)
    {
        vibrator.vibrate(30);
       
        sum="";
        countOne=0;//result=0;
        
        result=0f;
        result_mul=1f;
        result_div=1f;
        press=' ';
        c=0;
        
        editText.setText(result.toString());
    }
    
    
    public void onClickListener_sin(View v)
    {
        vibrator.vibrate(30);
        
        EditTextMsg= editText.getText().toString(); 
        doubleEditTextMsg=Double.parseDouble(EditTextMsg);//degree
        
        toRadian_doubleEditTextMsg=Math.toRadians(doubleEditTextMsg);
        
        afterSin =Math.sin(toRadian_doubleEditTextMsg);
        
        editText.setText(afterSin.toString());
        
        EditTextMsg=editText.getText().toString();
        result=Float.parseFloat(EditTextMsg);
        
        result_mul=Float.parseFloat(EditTextMsg);
        
        result_div=Float.parseFloat(EditTextMsg);
        
        
        sum="";
        
        
    }
    
    public void onClickListener_cos(View v)
    {
        vibrator.vibrate(30);
        EditTextMsg= editText.getText().toString(); 
        doubleEditTextMsg=Double.parseDouble(EditTextMsg);//degree
        
        toRadian_doubleEditTextMsg=Math.toRadians(doubleEditTextMsg);
        
        after_cos =Math.cos(toRadian_doubleEditTextMsg);
        
        editText.setText(after_cos.toString());
        
        EditTextMsg=editText.getText().toString();
        result=Float.parseFloat(EditTextMsg);
        
        result_mul=Float.parseFloat(EditTextMsg);
        
        result_div=Float.parseFloat(EditTextMsg);
        
        
        sum="";
        
        
    }
    
    public void onClickListener_tan(View v)
    {
        vibrator.vibrate(30);
        
        EditTextMsg= editText.getText().toString(); 
        doubleEditTextMsg=Double.parseDouble(EditTextMsg);//degree
        
        toRadian_doubleEditTextMsg=Math.toRadians(doubleEditTextMsg);
        
        after_tan =Math.tan(toRadian_doubleEditTextMsg);
        
        editText.setText(after_tan.toString());
        
        EditTextMsg=editText.getText().toString();
        result=Float.parseFloat(EditTextMsg);
        
        result_mul=Float.parseFloat(EditTextMsg);
        
        result_div=Float.parseFloat(EditTextMsg);
        
        
        sum="";
    }
    
    
    public void onClickListener_squared_2(View v)
    {
        vibrator.vibrate(30);
        
        EditTextMsg= editText.getText().toString(); 
        doubleEditTextMsg=Double.parseDouble(EditTextMsg);//degree
        
        
        after_squared_2 =Math.pow(doubleEditTextMsg,2);
        
        editText.setText(after_squared_2.toString());
        
        EditTextMsg=editText.getText().toString();
        result=Float.parseFloat(EditTextMsg);
        
        result_mul=Float.parseFloat(EditTextMsg);
        
        result_div=Float.parseFloat(EditTextMsg);
        
        
        sum="";
        
        
    }
    
    public void onClickListener_qube(View v)
    {
        vibrator.vibrate(30);
        
        EditTextMsg= editText.getText().toString(); 
        doubleEditTextMsg=Double.parseDouble(EditTextMsg);//degree
        
        
        after_qube=Math.pow(doubleEditTextMsg,3);
        
        editText.setText(after_qube.toString());
        
        EditTextMsg=editText.getText().toString();
        result=Float.parseFloat(EditTextMsg);
        
        result_mul=Float.parseFloat(EditTextMsg);
        
        result_div=Float.parseFloat(EditTextMsg);
        
        
        sum="";
        
        
    }
    
     public void onClickListener_root(View v)
    {
         vibrator.vibrate(30);
        EditTextMsg= editText.getText().toString(); 
        doubleEditTextMsg=Double.parseDouble(EditTextMsg);//degree
        
        after_root =Math.sqrt(doubleEditTextMsg);
        
        editText.setText(after_root.toString());
        
        EditTextMsg=editText.getText().toString();
        result=Float.parseFloat(EditTextMsg);
        
        result_mul=Float.parseFloat(EditTextMsg);
        
        result_div=Float.parseFloat(EditTextMsg);
        
        
        sum="";
        
    }
     
     public void onClickListener_pi(View v)
     {
          vibrator.vibrate(30);
          
          if(press=='=')
          {
              onClickListenerReset(buttonReset);
          }
          
          sum=pi;
         
         editText.setText(pi);
      
     } 
     
     public void onClickListener_del(View v)
     {
         vibrator.vibrate(30);
         if(sum !="")
         {
             StringBuilder stringBuilder=new StringBuilder(80);
             
             stringBuilder.append(sum);
             
             sum=stringBuilder.deleteCharAt(stringBuilder.length()-1).toString();
             
             editText.setText(sum);
         }
         
     }
     
     public void onClickListener_dec(View v)
     {
         vibrator.vibrate(30);
         
         EditTextMsg= editText.getText().toString();
         
         for(int i=0;i<=EditTextMsg.length()-1;i++)
         {
             unicode_value=EditTextMsg.codePointAt(i);
             if(unicode_value>49 || unicode_value<48)
             {
                 dec_flag=1;
                 Log.d("uni",unicode_value.toString());
                 break;
             }
             

         }
         
         
         if(dec_flag==0)
         {
             dec_num=Integer.parseInt(EditTextMsg, 2);
             
             
             editText.setText(dec_num.toString());
             
             EditTextMsg=editText.getText().toString();
             
             sum="";
         }
         else
         {
                 
                 editText.setText("input error");
                 
                 sum="";
         }
             
     } 
    
     public void onClickListener_bin(View v)
     {
         vibrator.vibrate(30);
         //button_bin.setBackgroundColor(Color.BLUE);
         EditTextMsg= editText.getText().toString();
         
         for(i=0;i<EditTextMsg.length();i++)
         {
             if(EditTextMsg.charAt(i)=='.')
             {
                 break;
             }
             else
             {
                 dec_string=dec_string+EditTextMsg.charAt(i);
             }
         }
         dec_num=Integer.parseInt(dec_string);
         
         Log.d("dec_num=",dec_num.toString());
         
         bin_num=Integer.toBinaryString(dec_num);
         
         editText.setText(bin_num);
         
         dec_string="";
         EditTextMsg="";
         bin_num="";
         sum="";
         
     }
     
     public void onClickListener_hex(View v)
     {
         vibrator.vibrate(30);
         
         EditTextMsg= editText.getText().toString();
         
         for(i=0;i<EditTextMsg.length();i++)
         {
             if(EditTextMsg.charAt(i)=='.')
             {
                 break;
             }
             else
             {
                 hex_string=hex_string+EditTextMsg.charAt(i);
             }
         }
         dec_num=Integer.parseInt(hex_string);
         
         Log.d("dec_num=",dec_num.toString());
         
         hex_num=Integer.toHexString(dec_num);
         
         editText.setText(hex_num);
         
         dec_string="";
         hex_string="";
         
         EditTextMsg="";
         
         bin_num="";
         hex_num="";
         
         sum="";
         
     }
     
     public void onClickListener_oct(View v)
     {
         vibrator.vibrate(30);
         
         EditTextMsg= editText.getText().toString();
         
         for(i=0;i<EditTextMsg.length();i++)
         {
             if(EditTextMsg.charAt(i)=='.')
             {
                 break;
             }
             else
             {
                 oct_string=oct_string+EditTextMsg.charAt(i);
             }
         }
         dec_num=Integer.parseInt(oct_string);
         
         Log.d("dec_num=",dec_num.toString());
         
         oct_num=Integer.toOctalString(dec_num);
         
         editText.setText(oct_num);
         
         dec_string="";
         hex_string="";
         oct_string="";
         
         EditTextMsg="";
         
         bin_num="";
         hex_num="";
         oct_num="";
         
         sum="";
         
     }
}
