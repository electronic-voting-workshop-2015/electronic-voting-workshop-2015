require 'models/commitment'
require 'models/message'
require 'models/complaint'

def publish_and_retrieve( model, publish_url, retrieve_url )
    post publish_url do
        publication = model.create!( content: params[ "content" ].to_json )
    end

    get retrieve_url do
        all = model.all.to_a.map do |publication| 
            hash = JSON.parse publication.content
            hash
        end
        all.to_json
    end
end

publish_and_retrieve Commitment, '/publishCommitment', '/retrieveCommitment'
publish_and_retrieve Message, '/publishMessage', '/retrieveMessage'
publish_and_retrieve Complaint, '/publishComplaint', '/retrieveComplaint'
