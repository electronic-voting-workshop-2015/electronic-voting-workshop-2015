def verify_certificate( first, second, content, signature )
    # PUT VERIFICATION CODE HERE
	pythonOutput = `python3 ../ThresholdCryptography/main.py "verifyCertificate" #{first} #{second} "#{content}" "#{signature}"`
    verified = pythonOutput == "true"
    raise "invalid signature!" if ! verified
end

def demand_valid_signature!( request )
  return if ! Configuration.verify_signature
  return if ENV['BB_INSECURE']
  puts "VERIFY SIGNATURE ON THESE PARAMTERS: #{params}"
  params = request.params
  publicKey = PublicKey.find params[ "party_id" ]
  verify_certificate publicKey.first, publicKey.second, params[ "content" ], params[ "signature" ]
end 
